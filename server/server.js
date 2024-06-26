const express = require("express");
const multer = require("multer");
const bodyParser = require("body-parser");
const fs = require("fs");
const path = require("path");
const { exec, execSync } = require("child_process");
const { info, active } = require("./log");
const { selectFile, getDeviceId } = require("./tools");

const server = express();
const upload = multer({ dest: "jennyGenerated/" });

let imagePath = "";

server.use(bodyParser.json());
server.use(bodyParser.urlencoded({ extended: true }));

server.get("/send_file_to_phone", (req, res) => {
  selectFile((filePath) => {
    deviceId = getDeviceId();
    active(`run adb -s ${deviceId} push ${filePath} /sdcard/Download`);
    execSync(`adb -s ${deviceId} push ${filePath} /sdcard/Download`);
    res.send({
      msg: "ok",
      filePath: filePath,
    });
  });
});

server.post("/command_string", (req, res) => {
  info("command = IDENTIFY_STRING");
  const fullMessage = req.body.content;
  active(fullMessage);
  const fileName = `jennyGenerated/${Date.now()}.txt`;
  fs.mkdirSync(path.dirname(fileName), { recursive: true });
  fs.writeFileSync(fileName, fullMessage);
  active(`打开文件 ${fileName}`);
  exec(`open ${fileName}`);
  res.json({ msg: "ok" });
});

server.post("/command_send_image_byte", upload.single("photo"), (req, res) => {
  info("command = IDENTIFY_SEND_IMAGE_BYTE");
  active("获取图片 ing...");
  const fileName = `jennyGenerated/${Date.now()}.png`;
  fs.renameSync(req.file.path, fileName);
  active(`打开图片 ${fileName}`);
  exec(`open -R ${fileName}`);
  imagePath = fileName;
  res.json({ msg: "ok" });
});

server.post("/command_send_video_byte", upload.single("video"), (req, res) => {
  info("command = IDENTIFY_SEND_VIDEO_BYTE");
  active("获取视频 ing...");
  const fileName = `jennyGenerated/${Date.now()}.mp4`;
  fs.renameSync(req.file.path, fileName);
  active(`打开视频 ${fileName}`);
  exec(`open -R ${fileName}`);
  res.json({ msg: "ok" });
});

server.post("/command_send_file_adb_pull", (req, res) => {
  info("command = IDENTIFY_SEND_FILE_ADB_PULL");
  let commandStr = req.body.content;
  if (!fs.existsSync("jennyGenerated")) {
    fs.mkdirSync("jennyGenerated");
  }
  const commandStrArray = commandStr.split(" ");
  const adbIndex = commandStrArray.indexOf("adb");
  commandStrArray.splice(adbIndex + 1, 0, "-s", deviceId);
  commandStr = commandStrArray.join(" ");
  active(`使用adb pull 方式获取文件 ${commandStr}`);
  execSync(`cd jennyGenerated && ${commandStr}`);
  res.json({ msg: "ok" });
});

module.exports = server;
