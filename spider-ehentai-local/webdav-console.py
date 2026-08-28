# -*- coding: utf-8 -*-
"""
WebDAV 共享管理控制台
- 查看 / 添加 / 删除挂载路径（wsgidav-config.yaml 的 provider_mapping）
- 启动 / 停止 WebDAV 服务
"""
import os
import re
import socket
import subprocess
import sys

from ruamel.yaml import YAML

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_PATH = os.path.join(BASE_DIR, "wsgidav-config.yaml")
WSGIDAV_EXE = r"C:\Users\zpl\AppData\Local\Python\pythoncore-3.14-64\Scripts\wsgidav.exe"
LOG_PATH = os.path.join(BASE_DIR, "webdav.log")

yaml = YAML()
yaml.preserve_quotes = True


def load_config():
    if not os.path.exists(CONFIG_PATH):
        print("配置文件不存在: %s" % CONFIG_PATH)
        return None
    with open(CONFIG_PATH, "r", encoding="utf-8") as f:
        return yaml.load(f)


def save_config(cfg):
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        yaml.dump(cfg, f)


def get_port(cfg):
    return int(cfg.get("port", 8088))


def list_mounts(cfg):
    mapping = cfg.get("provider_mapping", {})
    if not mapping:
        print("  当前没有任何挂载路径")
        return
    print("  %-14s %s" % ("访问路径", "本地目录"))
    print("  " + "-" * 60)
    for route, conf in mapping.items():
        if route.startswith(":"):  # 内置资源（dir_browser）跳过
            continue
        root = conf.get("root", "?") if isinstance(conf, dict) else str(conf)
        print("  %-14s %s" % (route, root))


def add_mount(cfg):
    route = input("访问路径（如 /video，回车取消）: ").strip()
    if not route:
        return
    if not route.startswith("/") or route.count("/") != 1 or route == "/":
        print("访问路径格式错误：须为单个 / 开头的路径，如 /video")
        return
    if " " in route:
        print("访问路径不能包含空格")
        return
    mapping = cfg.get("provider_mapping", {})
    if route in mapping:
        print("路径 %s 已存在" % route)
        return
    root = input("本地目录（如 E:\\video，回车取消）: ").strip().rstrip("\\/")
    if not root:
        return
    if not os.path.isdir(root):
        print("目录不存在或不可访问: %s" % root)
        return
    mapping[route] = {"root": root.replace("/", "\\")}
    cfg["provider_mapping"] = mapping
    save_config(cfg)
    print("已添加: %s -> %s（重启服务后生效）" % (route, root))


def remove_mount(cfg):
    mapping = cfg.get("provider_mapping", {})
    routes = [r for r in mapping if not r.startswith(":") and r != "/"]
    if not routes:
        print("  没有可删除的挂载路径")
        return
    print("  可删除的挂载路径:")
    for i, r in enumerate(routes, 1):
        print("    %d. %s -> %s" % (i, r, mapping[r].get("root", "?")))
    choice = input("输入序号删除（回车取消）: ").strip()
    if not choice.isdigit():
        return
    idx = int(choice)
    if idx < 1 or idx > len(routes):
        print("序号无效")
        return
    route = routes[idx - 1]
    confirm = input("确认删除 %s ？(y/N): " % route).strip().lower()
    if confirm != "y":
        print("已取消")
        return
    del mapping[route]
    cfg["provider_mapping"] = mapping
    save_config(cfg)
    print("已删除: %s（重启服务后生效）" % route)


def is_running(port):
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(2)
        result = s.connect_ex(("127.0.0.1", port))
        s.close()
        return result == 0
    except Exception:
        return False


def start_service(cfg):
    port = get_port(cfg)
    if is_running(port):
        print("服务已在运行（端口 %d），无需重复启动" % port)
        return
    if not os.path.exists(WSGIDAV_EXE):
        print("未找到 wsgidav: %s" % WSGIDAV_EXE)
        return
    logf = open(LOG_PATH, "w", encoding="utf-8")
    env = dict(os.environ)
    env["PYTHONPATH"] = BASE_DIR + os.pathsep + env.get("PYTHONPATH", "")
    proc = subprocess.Popen(
        [WSGIDAV_EXE, "-c", CONFIG_PATH],
        cwd=BASE_DIR,
        stdout=logf,
        stderr=subprocess.STDOUT,
        env=env,
        creationflags=subprocess.CREATE_NO_WINDOW,
    )
    # 等待端口就绪
    import time
    for _ in range(10):
        time.sleep(1)
        if is_running(port):
            print("服务已启动: http://0.0.0.0:%d （日志: webdav.log）" % port)
            return
    print("服务启动中（PID %d），端口 %d 未就绪，日志见 webdav.log" % (proc.pid, port))


def stop_service(cfg):
    port = get_port(cfg)
    if not is_running(port):
        print("服务未在运行")
        return
    script = (
        "Get-CimInstance Win32_Process -Filter \"Name like '%%python%%'\" | "
        "Where-Object { $_.CommandLine -like '*wsgidav*' } | "
        "ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"
    )
    subprocess.run(["powershell.exe", "-Command", script], capture_output=True)
    import time
    time.sleep(2)
    if is_running(port):
        print("停止失败，端口 %d 仍被占用" % port)
    else:
        print("服务已停止")


def show_status(cfg):
    port = get_port(cfg)
    if is_running(port):
        print("服务状态: 运行中（端口 %d，http://0.0.0.0:%d）" % (port, port))
    else:
        print("服务状态: 未运行")
    list_mounts(cfg)


def main():
    print("=" * 52)
    print("  WebDAV 共享管理控制台")
    print("  配置文件: %s" % CONFIG_PATH)
    print("=" * 52)
    while True:
        cfg = load_config()
        if cfg is None:
            break
        port = get_port(cfg)
        status = "运行中" if is_running(port) else "已停止"
        print("")
        print("  [服务状态: %s  端口: %d]" % (status, port))
        print("  ------------------------------------------")
        print("   1. 查看挂载路径")
        print("   2. 添加挂载路径")
        print("   3. 删除挂载路径")
        print("   4. 启动 WebDAV 服务")
        print("   5. 停止 WebDAV 服务")
        print("   0. 退出")
        print("  ------------------------------------------")
        choice = input("请选择: ").strip()
        try:
            if choice == "1":
                list_mounts(cfg)
            elif choice == "2":
                add_mount(cfg)
            elif choice == "3":
                remove_mount(cfg)
            elif choice == "4":
                start_service(cfg)
            elif choice == "5":
                stop_service(cfg)
            elif choice == "0":
                print("再见")
                break
            else:
                print("无效选择")
        except KeyboardInterrupt:
            print("\n操作已取消")
        except Exception as e:
            print("出错了: %s" % e)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n再见")
        sys.exit(0)
