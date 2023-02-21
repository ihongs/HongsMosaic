# 简单微站构建平台

* 作者: Kevin Hongs
* 邮箱: kevin.hongs@gmail.com
* 授权: MIT License

这是一个小型建站平台，开在自己的账号内自行搭建页面，利用链接把多个功能页组织起来行成一个微型站点。可构建的页面有：面板（综合功能分类入口页）、内容（文章和新闻等）、表单（投票、问卷）等。

将来也可能跟我的另一个项目 [HongsMasque](https://github.com/ihongs/HongsMasque) 进行结合，提供聊天室、客服等组件。

## 部署方式

本项目代码依赖 HongsCORE,HongsCRAP 两个项目, 故需先获取并构建这两个系统. 完整流程如下:

```bash
# 构建 HongsCORE
git clone https://github.com/ihongs/HongsCORE.git
cd HongsCORE
mvn package
cd ..

# 构建 HongsCRAP
git clone https://github.com/ihongs/HongsCRAP.git
cd HongsCRAP
mvn package
cd ..

# 构建、设置并启动
git clone https://github.com/ihongs/HongsMosaic.git
cd HongsMosaic
mvn package
cd hongs-mosaic-web/target/HongsMosaic
# 初始设置
sh bin/hdo source setup --DEBUG 0
# 启动服务
sh bin/hdo server.start --DEBUG 1
```

可以将构建的 `hongs-mosaic-web/target/HongsMosaic` 拷贝出来, 这是最终的应用目录. 也可以发邮件到 kevin.hongs@gmail.com 索要构建好的包(JDK 请自行下载和安装).

Windows 下进入此项目文件夹, 双击 `setup.bat` 即可完成设置, 双击 `start.bat` 立即启动服务; 注意: Windows 下如需关闭服务程序, 务必要在命令窗口按 `Ctrl+C` 中止进程, 不要直接关闭命令窗口. 后者导致下次无法启动, 可尝试删除 `var/server/8080.pid` 后重新启动.

浏览器打开 <http://localhost:8080/> 可进入后台.
