# notebook
Homework Project.

## Requirements
* Android SDK 26+
* Android Debugging Tools (edit app/build.graddle according to your version)
* bmob cloud service (registered by zqzqz, application id: 5caf9bb9c09cbce2a86def8cd9c97ee2)

## Structure
Using bmob as backend database which leverages user authentication and text storage. Local SQLite serves as cache database.
* ./java/bean : Objects definition
* ./java/ui : Activities
* ./java/db : SQLite configurations and handler
* ./java/utils : Other tools
* ./res/layout : frontend pages

## TODO
* Fix AES bug, enable encryption
* Implement tone analyzer and similarity analyzer.
* Add user profile page. It is better to have a calendar.

原作者APK下载链接 http://bmob-cdn-14889.b0.upaiyun.com/2017/11/07/9ac34dd140c2fe93803580f9b3fcb3bd.apk
