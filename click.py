import subprocess


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


if __name__ == "__main__":
    devices = get_connected_devices()
    if devices:
        selected_device_id = select_device(devices)
        print(f"You selected device: {selected_device_id}")
    else:
        print("No devices connected.")
