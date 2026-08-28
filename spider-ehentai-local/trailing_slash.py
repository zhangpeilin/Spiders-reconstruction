# -*- coding: utf-8 -*-
"""
WsgiDAV middleware: 为无尾斜杠的子挂载路径补上尾斜杠。

修复 wsgidav 4.3.5 缺陷：访问 /movie（无尾斜杠）时 provider 匹配后
路径余量为空，dir_browser 把空路径重定向到根目录（/），而不是补斜杠。
本中间件在请求进入前拦截：非根挂载路径的精确匹配请求 301 到 path + "/"。
"""
import logging

from wsgidav.mw.base_mw import BaseMiddleware

_logger = logging.getLogger("wsgidav")

__all__ = ["TrailingSlashMiddleware"]


class TrailingSlashMiddleware(BaseMiddleware):
    def __init__(self, wsgidav_app, next_app, config):
        super().__init__(wsgidav_app, next_app, config)

    def __call__(self, environ, start_response):
        # WsgiDAVApp 主逻辑会把挂载前缀从 PATH_INFO 移到 SCRIPT_NAME，
        # 原始请求路径 = SCRIPT_NAME + PATH_INFO
        script = environ.get("SCRIPT_NAME", "")
        path = environ.get("PATH_INFO", "")
        full_path = script + path
        method = environ.get("REQUEST_METHOD", "GET")
        if method in ("GET", "HEAD", "PROPFIND") and full_path and not full_path.endswith("/"):
            shares = getattr(self.wsgidav_app, "sorted_share_list", None) or []
            for share in shares:
                if share == "/":
                    continue
                if full_path.lower() == share:
                    _logger.info("TrailingSlash: %s -> %s/", full_path, full_path)
                    start_response(
                        "301 Moved Permanently",
                        [("Location", full_path + "/"), ("Content-Length", "0")],
                    )
                    return []
        return self.next_app(environ, start_response)
