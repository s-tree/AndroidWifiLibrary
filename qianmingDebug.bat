java -jar ../qianming/signapk.jar ../qianming/platform.x509.pem ../qianming/platform.pk8 build/outputs/apk/debug/app-debug.apk build/outputs/apk/debug/app-signed.apk
adb -s PA0RN50901 install -r -t -d build/outputs/apk/debug/app-signed.apk
adb -s PA0RN50901 shell am start -n com.jingxi.smartlife.wifi/.MainActivity