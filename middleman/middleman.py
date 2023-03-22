import sys
import time
# from networktables import NetworkTables
import socket
import struct
import logging
import os

logging.basicConfig(level=logging.DEBUG)

# ip = "10.55.72.2"

port = 1234

os.system("adb tcpip {}".format(port))
os.system("adb forward tcp:{0} tcp:{0}".format(port))

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect(("127.0.0.1", port))
    while True:
        ret = s.recv(1)
        print(ret)
        mode = struct.unpack('>c', ret)
        if mode == 0x01:
            row, col = struct.unpack('>cc', s.recv(2))
            print('{},{}'.format(row, col))
        elif mode == 0x02:
            slider = struct.unpack('>d', s.recv(8))
            print(slider)
        pass

# NetworkTables.initialize(server=ip)

# sd = NetworkTables.getTable("SmartDashboard")


