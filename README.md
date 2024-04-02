# Jenny
方便测试机上传数据到电脑端。目前支持 `图片上传` 和 `文字上传`

# 原理
通过`socket`方式实现，利用局域网内同一网络下可访问`ip`，在电脑端创建本地服务器，测试机通过监听电脑端ip 和 端口，实现数据上传到电脑

# 使用
`sh start.sh` 进入 `初始化` 页面：
host无序修改，port默认端口为`40006`，如默认端口`40006`被占用，改用当前电脑无占用的端口即可：
![image](https://github.com/gfzy9876/Jenny/assets/34124544/9ae6ba0a-f089-4499-b0b3-6a6863d2d1d8)


# 上传文字
测试机复制需要上传的文字，然后再粘贴到输入框中，点击`发送字符串`即可：
<img width="665" alt="image" src="https://github.com/gfzy9876/Jenny/assets/34124544/100a3626-a618-4600-b2e2-85354756a2e9">
如图，绿色文字即为复制的字符串：
![image](https://github.com/gfzy9876/Jenny/assets/34124544/cc52e42c-96ad-4350-bb36-5573cc48c89f)

# 上传图片
点击`发送图片`按钮：
<img width="665" alt="image" src="https://github.com/gfzy9876/Jenny/assets/34124544/992fa62c-f098-461b-895e-c2c857119f5a">
选择完图片之后即可上传到电脑端并打开文件：
![image](https://github.com/gfzy9876/Jenny/assets/34124544/09ab25b3-6d75-42fb-b3c0-1cb107055cab)


