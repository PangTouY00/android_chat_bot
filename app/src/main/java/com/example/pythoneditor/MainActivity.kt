import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.jgit.api.Git
import java.io.File
import com.example.pythoneditor.service.UpdateChecker
import com.example.pythoneditor.dialog.UpdateDialog
import kotlinx.coroutines.withContext
import com.amrdeveloper.codeview.CodeView
import android.graphics.Color
import com.example.pythoneditor.editor.PythonKeywordsProvider

class MainActivity : AppCompatActivity() {
    private lateinit var codeEditor: CodeView
    private lateinit var outputText: EditText
    private lateinit var runButton: Button
    private lateinit var installPackageButton: Button
    private lateinit var packageNameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 请求权限
        requestPermissions()

        // 检查更新
        checkUpdate()

        // 初始化Python环境
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        codeEditor = findViewById(R.id.codeEditor)
        outputText = findViewById(R.id.outputText)
        runButton = findViewById(R.id.runButton)
        installPackageButton = findViewById(R.id.installPackageButton)
        packageNameInput = findViewById(R.id.packageNameInput)
        
        setupCodeEditor()
    }

    private fun setupCodeEditor() {
        // 设置代码高亮
        codeEditor.apply {
            setEnableLineNumber(true)
            setLineNumberTextColor(Color.GRAY)
            setLineNumberTextSize(25f)
            
            // 设置关键字高亮
            addSyntaxPattern(
                PythonKeywordsProvider.PYTHON_KEYWORDS.joinToString("|").toPattern(),
                Color.parseColor("#FF6D00")
            )
            
            // 设置内置函数高亮
            addSyntaxPattern(
                PythonKeywordsProvider.PYTHON_BUILT_INS.joinToString("|").toPattern(),
                Color.parseColor("#2962FF")
            )
            
            // 设置字符串高亮
            addSyntaxPattern(
                "\".*\"".toPattern(),
                Color.parseColor("#00C853")
            )
            
            // 设置数字高亮
            addSyntaxPattern(
                "\\b\\d+\\b".toPattern(),
                Color.parseColor("#FF1744")
            )
            
            // 设置注释高亮
            addSyntaxPattern(
                "#.*".toPattern(),
                Color.GRAY
            )
        }

        // 设置代码补全
        val suggestions = (PythonKeywordsProvider.PYTHON_KEYWORDS + 
                         PythonKeywordsProvider.PYTHON_BUILT_INS).sorted()
        
        codeEditor.apply {
            setSuggestionData(suggestions)
            setEnableAutoComplete(true)
            setAutoCompleteItemClickListener { item ->
                val currentText = text.toString()
                val cursorPosition = selectionStart
                val beforeCursor = currentText.substring(0, cursorPosition)
                val afterCursor = currentText.substring(cursorPosition)
                
                val lastWord = beforeCursor.split(Regex("\\s+")).lastOrNull() ?: ""
                val replacement = if (lastWord.isNotEmpty()) {
                    beforeCursor.substring(0, beforeCursor.length - lastWord.length) + item + afterCursor
                } else {
                    beforeCursor + item + afterCursor
                }
                
                setText(replacement)
                setSelection(cursorPosition - lastWord.length + item.length)
            }
        }
    }

    private fun setupListeners() {
        runButton.setOnClickListener {
            runPythonCode()
        }

        installPackageButton.setOnClickListener {
            installPackage()
        }

        // 添加清空按钮监听器
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            codeEditor.setText("")
            outputText.setText("")
        }
    }

    private fun runPythonCode() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val py = Python.getInstance()
                val pyCode = codeEditor.text.toString()
                val result = py.getModule("__main__").callAttr("exec", pyCode)
                
                runOnUiThread {
                    outputText.setText(result.toString())
                }
            } catch (e: Exception) {
                runOnUiThread {
                    outputText.setText("错误: ${e.message}")
                }
            }
        }
    }

    private fun installPackage() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageName = packageNameInput.text.toString()
                val py = Python.getInstance()
                py.getModule("pip").callAttr("main", arrayOf("install", packageName))
                
                runOnUiThread {
                    outputText.setText("成功安装包: $packageName")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    outputText.setText("安装失败: ${e.message}")
                }
            }
        }
    }

    private fun initGitRepo(repoPath: String, remoteUrl: String) {
        try {
            Git.init().setDirectory(File(repoPath)).call()
            val git = Git.open(File(repoPath))
            git.remoteAdd()
                .setName("origin")
                .setUri(org.eclipse.jgit.transport.URIish(remoteUrl))
                .call()
        } catch (e: Exception) {
            outputText.setText("Git初始化失败: ${e.message}")
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 1)
        }
    }

    private fun checkUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            val updateChecker = UpdateChecker(this@MainActivity)
            // 替换为你的实际仓库地址
            val updateUrl = "https://raw.githubusercontent.com/PangTouY00/android-python-editor/main/version.json"
            
            val versionInfo = updateChecker.checkUpdate(updateUrl)
            
            if (versionInfo != null) {
                withContext(Dispatchers.Main) {
                    UpdateDialog(this@MainActivity).show(versionInfo)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // 权限都已授予
            } else {
                // 部分权限被拒绝
                outputText.setText("警告：部分权限被拒绝，可能影响程序功能")
            }
        }
    }
} 
