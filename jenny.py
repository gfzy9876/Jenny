import json
import os
import subprocess
import sys
import time

from flask import Flask, app, jsonify, request


class Colors:
    RED = "\033[91m"
    GREEN = "\033[92m"
    YELLOW = "\033[93m"
    BLUE = "\033[94m"
    MAGENTA = "\033[95m"
    CYAN = "\033[96m"
    RESET = "\033[0m"


def error(msg):
    print(Colors.RED + msg + Colors.RESET)


def warn(msg):
    print(Colors.YELLOW + msg + Colors.RESET)


def info(msg):
    print(Colors.RESET + msg + Colors.RESET)


def active(msg):
    print(Colors.GREEN + msg + Colors.RESET)


app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 160 * 1024 * 1024


@app.route("/command_string", methods=["POST"])
def handle_command_string():
    info(f"command = IDENTIFY_STRING\n")
    full_message = request.form.get("content")
    active(full_message)
    return {"msg": "ok"}


@app.route("/command_send_image_byte", methods=["POST"])
def handle_command_send_image_byte():
    info(f"command = IDENTIFY_SEND_IMAGE_BYTE\n")
    active("获取图片 ing...")
    file_name = f"jennyGenerated/{int(time.time())}.png"
    os.makedirs(os.path.dirname(file_name), exist_ok=True)
    request_file = request.files["photo"]
    request_file.save(file_name)
    active(f"打开图片 {file_name}")
    os.system(f"open -R {file_name}")
    return {"msg": "ok"}


@app.route("/command_send_video_byte", methods=["POST"])
def handle_command_send_video_byte():
    info(f"command = IDENTIFY_SEND_VIDEO_BYTE\n")
    file_name = f"jennyGenerated/{int(time.time())}.mp4"
    active("获取视频 ing...")
    os.makedirs(os.path.dirname(file_name), exist_ok=True)
    request_file = request.files["video"]
    request_file.save(file_name)
    active(f"打开视频 {file_name}")
    os.system(f"open -R {file_name}")
    return {"msg": "ok"}


@app.route("/command_send_file_adb_pull", methods=["POST"])
def handle_command_send_file_adb_pull():
    info(f"command = IDENTIFY_SEND_FILE_ADB_PULL\n")
    commandStr = request.form.get("content")
    if not os.path.exists("jennyGenerated"):
        os.makedirs("jennyGenerated")
    commandStrArray = commandStr.split(" ")
    adb_index = commandStrArray.index("adb")
    commandStrArray.insert(adb_index + 1, "-s")
    commandStrArray.insert(adb_index + 2, device_id)
    commandStr = " ".join(commandStrArray)
    active(f"使用adb pull 方式获取文件 {commandStr}")
    subprocess.run(f"cd jennyGenerated && {commandStr}", shell=True)
    return {"msg": "ok"}


def get_connected_devices():
    devices = []
    try:
        output = subprocess.check_output(["adb", "devices", "-l"]).decode("utf-8")
        lines = output.strip().split("\n")
        for line in lines[1:]:
            if line.strip() != "":
                parts = line.strip().split()
                if len(parts) >= 2:
                    device_id = parts[0]
                    device_name = " ".join(parts[1:])
                    devices.append((device_id, device_name))
                elif len(parts) == 1:
                    devices.append((parts[0], None))
    except subprocess.CalledProcessError:
        pass
    return devices


def select_device(devices):
    if len(devices) == 1:
        return devices[0][0]
    print("Connected devices:")
    for i, (device_id, device_name) in enumerate(devices, 1):
        if device_name:
            print(f"{i}. {device_name} ({device_id})")
        else:
            print(f"{i}. {device_id}")
    choice = input("Enter the number of the device you want to select: ")
    try:
        choice = int(choice)
        if 1 <= choice <= len(devices):
            return devices[choice - 1][0]  # 仅返回设备的ID
        else:
            print("Invalid choice. Please enter a valid number.")
            return select_device(devices)
    except ValueError:
        print("Invalid input. Please enter a number.")
        return select_device(devices)


def get_local_ip():
    try:
        output = subprocess.check_output(["ifconfig"]).decode("utf-8")
        for line in output.split("\n"):
            if "inet " in line and "127.0.0.1" not in line and "broadcast" in line:
                # 在这里添加额外的条件来排除其他接口，如 utunX
                if "utun" not in line:
                    return line.split()[1]
    except Exception as e:
        print("Error:", e)
    return "127.0.0.1"


pkg = ""


def check_app_installed(package_name: str):
    # 使用 adb shell pm list packages 命令获取设备上所有已安装应用程序的包名列表
    result = subprocess.run(
        ["adb", "-s", device_id, "shell", "pm", "list", "packages"],
        capture_output=True,
        text=True,
    )
    splitlines = result.stdout.splitlines()
    # 解析命令输出，检查是否包含指定包名
    return package_name in splitlines or f"package:{package_name}" in splitlines


# 指定要检查的包名
device_id = ""

if __name__ == "__main__":
    pkg = sys.argv[1]

    devices = get_connected_devices()
    if devices:
        device_id = select_device(devices)
        print(f"You selected device: {device_id}")
    else:
        print("No devices connected.")
        exit()

    local_ip = get_local_ip()
    host = input(f"请输入ip地址(默认为本机IPv4地址: {local_ip}): ") or local_ip
    port = int(input("请输入端口号(默认使用40006): ") or 40006)
    os.system("cat logo")
    active(f"host = {host}, port = {port}")
    if not check_app_installed("pers.zy.jenny"):
        active("未安装")
        os.system(f"adb -s {device_id} install -t Jenny.apk")

    os.system(
        f"adb -s {device_id} shell am start -n pers.zy.jenny/.JennyActivity -f 0x10000000 --es local_trans_host_name {host} --ei local_trans_port {port}"
    )
    app.run(host, port, debug=True, use_reloader=False)
