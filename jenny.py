import socket
import sys


def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    host = "localhost"
    port = 40007
    try:
        server_socket.bind((host, port))
        server_socket.listen(5)
        print(f"Listening on port {port}...")

        while True:
            client_socket, address = server_socket.accept()
            print(f"Connection from {address} has been established.")

            full_message = ""
            while True:
                part = client_socket.recv(1024).decode("utf-8")
                full_message += part
                if "<<<END" in full_message:
                    print(f"原始数据: \n{full_message}")
                    # 消息完全接收，处理消息
                    full_message = full_message.replace("<<<END", "")  # 去除终止符
                    print(f"Received complete message: {full_message}")
                    break

            client_socket.send("You are connected to the server!".encode("utf-8"))
            # client_socket.close()
    except KeyboardInterrupt:
        print("Server is shutting down.")
    finally:
        server_socket.close()
        print("Server socket closed.")


if __name__ == "__main__":
    start_server()
