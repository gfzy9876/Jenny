import os
import socket
import subprocess
import sys
import time

SPLIT = b">>>\n<<<"


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


# 根据command不同处理
def dispatch_command(command_bytes: bytes, content_bytes: bytes):
    commandStr = str(command_bytes, "utf-8")
    info(f"command = {commandStr}\n")
    if "IDENTIFY_STRING" == commandStr:
        active(str(content_bytes, "utf-8"))
    elif "IDENTIFY_SEND_FILE_ADB_PULL" == commandStr:
        commandStr = str(content_bytes, "utf-8")
        if not os.path.exists("jennyGenerated"):
            os.makedirs("jennyGenerated")
        commandStrArray = commandStr.split(" ")
        adb_index = commandStrArray.index("adb")
        commandStrArray.insert(adb_index + 1, "-s")
        commandStrArray.insert(adb_index + 2, device_id)
        commandStr = " ".join(commandStrArray)
        active(f"使用adb pull 方式获取文件 {commandStr}")
        subprocess.run(f"cd jennyGenerated && {commandStr}", shell=True)
    elif "IDENTIFY_SEND_IMAGE_BYTE" == commandStr:
        active("获取图片 ing...")
        file_name = f"jennyGenerated/{int(time.time())}.png"
        os.makedirs(os.path.dirname(file_name), exist_ok=True)
        with open(file_name, "wb") as f:
            f.write(content_bytes)
        active(f"打开图片 {file_name}")
        os.system(f"open {file_name}")
    elif "IDENTIFY_SEND_VIDEO_BYTE" == commandStr:
        active("获取视频 ing...")
        file_name = f"jennyGenerated/{int(time.time())}.mp4"
        os.makedirs(os.path.dirname(file_name), exist_ok=True)
        with open(file_name, "wb") as f:
            f.write(content_bytes)
        active(f"打开视频 {file_name}")
        os.system(f"open {file_name}")


# 获取bytes
def recv_exact_size(sock: socket, size=2048):
    buffer = b""
    while True:
        part = sock.recv(size)
        buffer += part
        if b"<<<END" in part:
            break
    return buffer


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


def start_server(host: str, port: int):
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    try:
        server_socket.bind((host, port))
        server_socket.listen(5)
        info(f"Listening on port {port}...")

        while True:
            client_socket, address = server_socket.accept()
            info(f"Connection from {address} has been established.\n")
            full_message = recv_exact_size(client_socket).replace(b"<<<END", b"")
            full_message_with_line = [x for x in full_message.split(SPLIT) if x != b""]
            dispatch_command(full_message_with_line[0], full_message_with_line[1])
            print("\n")
            client_socket.send("response: OK!".encode("utf-8"))
            client_socket.close()
    except KeyboardInterrupt:
        error("Server is shutting down.")
    finally:
        server_socket.close()
        info("Server socket closed.")


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
    start_server(host, port)
