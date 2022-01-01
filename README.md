# 秘密花园

一个为小伙伴谋福利的程序。

- 下载抖音无水印视频

    使用方法：复制分享链接，粘贴到程序顶部输入框。

- 下载快手无水印视频

    使用方法：复制分享链接，粘贴到程序顶部输入框。

## 下载地址

- [GitHub](https://github.com/grandiloquent/91porn-client/releases)
- [国内](https://lucidu.cn/api/obs/HuaYuan.apk)

## 格式

通过此程序下载的 m3u8 视频文件，只是简单的执行合并操作。如果用安卓以外的设备播放，可能会出现问题。但可以通过以下步骤一次转化为 mp4 格式化：
1. 下载  [ffmpeg](https://github.com/BtbN/FFmpeg-Builds/releases/tag/latest)
2. 在视频存放的目录打开命令行。
3. 依序执行以下命令

        mkdir output
        for /r %i in (*.mp4) do ffmpeg -i "%i" -vcodec libx264 "output\%~ni.mp4"

## 问题

如果视频无法解析，可能有如下几种原因：

1. 视频源已删除，可以通过复制视频页面地址，尝试用浏览器打开。
2. 视频源需要验证访问，可以通过复制页面地址，粘贴到程序顶部输入框，打开页面输入验证码后，刷新一次。
3. 网络不稳定。
