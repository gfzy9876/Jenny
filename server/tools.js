const { execSync } = require("child_process");
const applescript = require("applescript");
const keypress = require("keypress");
const { error, info, active } = require("./log");

const script = `
set file_path to POSIX path of (choose file)
return file_path
`;

function getConnectedDevices() {
  const devices = [];
  try {
    const output = execSync("adb devices -l").toString();
    const lines = output.trim().split("\n");
    for (const line of lines.slice(1)) {
      if (line.trim()) {
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 2) {
          const deviceId = parts[0];
          const deviceName = parts.slice(1).join(" ");
          devices.push({ deviceId, deviceName });
        } else if (parts.length === 1) {
          devices.push({ deviceId: parts[0], deviceName: null });
        }
      }
    }
  } catch (e) {
    console.error(e);
  }
  return devices;
}

function selectDevice(devices) {
  return new Promise((resolve) => {
    if (devices.length === 1) {
      resolve(devices[0].deviceId);
    } else {
      console.log("Connected devices:");
      devices.forEach((device, index) => {
        console.log(
          `${index + 1}. ${device.deviceName || device.deviceId} (${
            device.deviceId
          })`
        );
      });

      const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
      });

      rl.question(
        "Enter the number of the device you want to select: ",
        (choice) => {
          const selectedDevice = devices[parseInt(choice) - 1];
          rl.close();
          if (selectedDevice) {
            resolve(selectedDevice.deviceId);
          } else {
            console.log("Invalid choice.");
            process.exit(1);
          }
        }
      );
    }
  });
}

function getLocalIp() {
  try {
    const output = execSync("ifconfig").toString();
    const lines = output.split("\n");
    for (const line of lines) {
      if (
        line.includes("inet ") &&
        !line.includes("127.0.0.1") &&
        line.includes("broadcast") &&
        !line.includes("utun")
      ) {
        return line.split(" ")[1];
      }
    }
  } catch (e) {
    console.error(e);
  }
  return "127.0.0.1";
}

function keyboardListen(key, call) {
  // 使process.stdin开始监听键盘输入
  keypress(process.stdin);
  process.stdin.setRawMode(true);
  process.stdin.resume();
  process.stdin.setEncoding("utf-8");

  process.stdin.on("keypress", (ch, inputKey) => {
    if (inputKey && key && inputKey.name === key) {
      call();
    }
  });
}

function selectFile(call) {
  info("选择文件: ");
  applescript.execString(script, (err, rtn) => {
    if (err) {
      error(err);
    } else {
      // 输出文件名和路径
      active(`Selected file path: + ${rtn}`);
      call(rtn);
    }
  });
}

module.exports = {
  getConnectedDevices,
  selectDevice,
  getLocalIp,
  keyboardListen,
  selectFile,
};
