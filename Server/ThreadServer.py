#! /usr/bin/env python
# -*- coding: utf-8 -*-
# @author: Yuanqin Lu

import SocketServer
import socket
import cv2
import numpy as np
import sys
import threading
from PIL import Image
import StringIO


class ThreadedTCPRequestHandler( SocketServer.BaseRequestHandler):
    def handle(self):
        """
        @Override the handle method
        When accept a request from client
        Server intends to receive the image's array string and array's dimension
        string.
        """
        print "New Connect!"
        imageArray = recvImg(self.request)
        #showImage(imageArray)
        buildName = classify(imageArray)
        sendIntro(buildName, self.request)

class ThreadedTCPServer( SocketServer.ThreadingMixIn, SocketServer.TCPServer):
    pass

def recvall(req, count):
    """
    Receive string which length is count
    """
    buf = b''
    while count:
        newbuf = req.recv(count)
        if not newbuf:
            return None
        buf += newbuf
        count -= len(newbuf)
    return buf

def recvImg(req):
    """
    Receive the image array
    Return
    @data: image's numpy array
    """
    tmpBuf = recvall(req, 16)
    print "imageLength:%s" %tmpBuf
    imageArgs = tmpBuf.split('-')
    #print "imageLength's len:%d" %len(length)
    stringData = recvall(req, int(imageArgs[0]))
    #print stringData
    #shape = recvShape(req)
    #shape = (int(imageArgs[1]),int(imageArgs[2]))
    #data = np.fromstring(stringData, dtype='uint8').reshape(shape)
    data =np.fromstring(stringData, dtype='uint8')
    stringData = str(data)[1:-1]
    print stringData
    strIOBuf = StringIO.StringIO(stringData)
    strIOBuf.seek(0)
    data = Image.open(strIOBuf)
    #image = Image.open(str)

    #print data.size, data.format
    #print data
    return data

def recvShape(req):
    """
    Receive the image array's dimension
    Return
    A Tuple: the dimension of array
    """
    length = recvall(req, 16)
    stringShape = recvall(req, int(length))
    return eval(stringShape)

def sendIntro(type, req):
    """
    Send the build's introduction to client
    """
    intro = getIntro(type)
    msg = "{}: {}".format(type, intro)
    req.sendall(msg)

def classify(imageArray):
    """
    Classify the image, return a string.
    Parameter
    @imageArray: numpy.array
    Return
    @Type: string
    """
    # TODO: judge by caffe
    return 'A'

def showImage(imageArray):
    """
    Show the image for test
    """
    cv2.namedWindow(threading.current_thread().name)
    cv2.imshow(threading.current_thread().name, imageArray)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

def getIntro(type):
    """
    Get the image's introduction, return a string
    Parameter
    @type: string
    Return
    @intro: string
    """
    # TODO: finish a dict to return
    return 'This is test'


if __name__ == "__main__":
    HOST, PORT = sys.argv[1], int(sys.argv[2])

    server = ThreadedTCPServer((HOST, PORT), ThreadedTCPRequestHandler)

    # Start a thread with the server -- that thread will then start one
    # more thread for each request
    server_thread = threading.Thread(target = server.serve_forever)
    # Exit the server thread when the main thread terminates
    server_thread.daemon = True
    server_thread.start()
    while (raw_input("Input operate:").upper() != "EOF"):
        continue
    server.shutdown()
