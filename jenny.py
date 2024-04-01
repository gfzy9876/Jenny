import os
import socket
import time

SPLIT = b">>>\n<<<"


# 根据command不同处理
def dispatch_command(command_bytes: bytes, content_bytes: bytes):
    commandStr = str(command_bytes, "utf-8")
    print(f"command = {commandStr}\n")
    if "IDENTIFY_STRING" == commandStr:
        print(str(content_bytes, "utf-8"))
    elif "IDENTIFY_SEND_FILE_ADB_PULL" == commandStr:
        print("获取文件 adb_pull:")
        os.system(content_bytes)
    elif "IDENTIFY_SEND_FILE_BYTE" == commandStr:
        print("获取文件 ing...")
        file_name = f"{int(time.time())}.png"
        with open(file_name, "wb") as f:
            f.write(content_bytes)
        print(f"打开文件 {file_name}")
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


def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    host = "10.0.10.65"
    port = 40007
    try:
        server_socket.bind((host, port))
        server_socket.listen(5)
        print(f"Listening on port {port}...")

        while True:
            client_socket, address = server_socket.accept()
            print(f"Connection from {address} has been established.\n")
            full_message = recv_exact_size(client_socket).replace(b"<<<END", b"")
            full_message_with_line = [x for x in full_message.split(SPLIT) if x != b""]
            dispatch_command(full_message_with_line[0], full_message_with_line[1])
            print("12312")
            client_socket.send("response: OK!".encode("utf-8"))
            client_socket.close()
    except KeyboardInterrupt:
        print("Server is shutting down.")
    finally:
        server_socket.close()
        print("Server socket closed.")


if __name__ == "__main__":
    start_server()
