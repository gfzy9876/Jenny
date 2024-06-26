const { execSync } = require("child_process");
const readline = require("readline");
const fs = require("fs");
const { active, info, error } = require("./log");
const app = require("./server");
const {
  getConnectedDevices,
  selectDevice,
  getLocalIp,
  keyboardListen,
  selectFile,
} = require("./tools");

let deviceId = "";

function checkAppInstalled(packageName) {
  try {
    const result = execSync(
      `adb -s ${deviceId} shell pm list packages`
    ).toString();
    return (
      result.includes(packageName) || result.includes(`package:${packageName}`)
    );
  } catch (e) {
    console.error(e);
    return false;
  }
}

(async () => {
  const devices = getConnectedDevices();
  if (devices.length > 0) {
    deviceId = await selectDevice(devices);
    info(`You selected device: ${deviceId}`);
  } else {
    info("No devices connected.");
    process.exit();
  }

  const localIp = getLocalIp();

  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  rl.question(`请输入ip地址(默认为本机IPv4地址: ${localIp}): `, (hostInput) => {
    const host = hostInput || localIp;
    rl.question("请输入端口号(默认使用40006): ", (portInput) => {
      const port = parseInt(portInput) || 40006;
      launch(host, port);
    });
  });
})();

function launch(host, port) {
  active(`host = ${host}, port = ${port}`);

  if (!checkAppInstalled("pers.zy.jenny")) {
    active("未安装");
  } else {
    active("已安装, 覆盖安装");
  }

  execSync(`adb -s ${deviceId} install -t ../Jenny.apk`);
  execSync(
    `adb -s ${deviceId} shell am start -n pers.zy.jenny/.JennyActivity -f 0x10000000 --es local_trans_host_name ${host} --ei local_trans_port ${port}`
  );

  app.listen(port, host, () => {
    info(`Server running at http://${host}:${port}/`);
  });

  active("Press 'f' to trigger the event.");
  keyboardListen("f", () => {
    selectFile((filePath) => {
      active(`run adb -s ${deviceId} push ${filePath} /sdcard/Download`);
      execSync(`adb -s ${deviceId} push ${filePath} /sdcard/Download`);
    });
  });
}
