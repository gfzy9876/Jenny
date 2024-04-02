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
        active(str(content_bytes, "utf-8"))
    elif "IDENTIFY_SEND_FILE_BYTE" == commandStr:
        active("获取文件 ing...")
        file_name = f"jennyGenerated/{int(time.time())}.png"
        os.makedirs(os.path.dirname(file_name), exist_ok=True)
        with open(file_name, "wb") as f:
            f.write(content_bytes)
        active(f"打开文件 {file_name}")
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
        # 创建一个socket对象
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        # 尝试连接到一个不存在的地址，仅仅是为了触发操作系统提供本机IP
        s.connect(("10.255.255.255", 1))
        IP = s.getsockname()[0]
    except Exception:
        IP = "127.0.0.1"
    finally:
        s.close()
    return IP


pkg = ""


def check_app_installed(package_name: str):
    # 使用 adb shell pm list packages 命令获取设备上所有已安装应用程序的包名列表
    result = subprocess.run(
        ["adb", "shell", "pm", "list", "packages"], capture_output=True, text=True
    )
    splitlines = result.stdout.splitlines()
    # 解析命令输出，检查是否包含指定包名
    return package_name in splitlines or f"package:{package_name}" in splitlines


# 指定要检查的包名
package_name = "com.example.app"

if __name__ == "__main__":
    pkg = sys.argv[1]
    local_ip = get_local_ip()
    host = input(f"请输入ip地址(默认为本机IPv4地址: {local_ip}): ") or local_ip
    port = int(input("请输入端口号(默认使用40006): ") or 40006)
    os.system("cat logo")
    active(f"host = {host}, port = {port}")
    # os.system(
    #     f"adb shell am start -n {pkg}/com.lingyue.debug.jennytrans.JennyActivity -f 0x10000000 --es local_trans_host_name {host} --ei local_trans_port {port}"
    # )
    if not check_app_installed("pers.zy.jenny"):
        active("未安装")
        os.system(f"adb install -t Jenny.apk")

    os.system(
        f"adb shell am start -n pers.zy.jenny/.JennyActivity -f 0x10000000 --es local_trans_host_name {host} --ei local_trans_port {port}"
    )
    start_server(host, port)
