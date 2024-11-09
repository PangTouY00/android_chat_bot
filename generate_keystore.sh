#!/bin/bash

# 设置变量
STORE_PASSWORD="android"
KEY_PASSWORD="android"
KEY_ALIAS="release_key"
KEYSTORE_PATH="release-key.jks"

# 生成keystore
keytool -genkey -v \
  -keystore $KEYSTORE_PATH \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias $KEY_ALIAS \
  -storepass $STORE_PASSWORD \
  -keypass $KEY_PASSWORD \
  -dname "CN=YourName, OU=YourOrganizationUnit, O=YourOrganization, L=YourCity, ST=YourState, C=CN"

# 将keystore转换为base64
SIGNING_KEY=$(base64 -w 0 $KEYSTORE_PATH)

# 输出信息
echo "请将以下信息添加到GitHub Secrets："
echo "-----------------------------------"
echo "SIGNING_KEY=$SIGNING_KEY"
echo "KEY_ALIAS=$KEY_ALIAS"
echo "KEY_STORE_PASSWORD=$STORE_PASSWORD"
echo "KEY_PASSWORD=$KEY_PASSWORD"
echo "-----------------------------------" 