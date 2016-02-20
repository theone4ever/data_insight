#!/usr/bin/python


import json
import urllib2
import os
import errno

import time, threading
from datetime import datetime

import sys

API_NAME = "SL_FAULT_INFO"
URL = 'http://api.sl.se/api2/deviations.json?key='
DEST_ROOT = './'
INTERVAL = 5 * 60


def timer():

    result = json.load(urllib2.urlopen(URL))
    print(time.ctime()+" send " + URL)
    if result['StatusCode'] == 0:
        json_string = json.dumps(result)
        now = datetime.now()
        file_name = now.strftime('%y-%m-%d-%H-%M-%S.json')
        mypath = DEST_ROOT + API_NAME + "/" + str(now.year) + "/" + str(now.month) + "/" + str(now.day)
        mkdir_p(mypath)
        target = open(mypath + "/" + file_name, 'w')
        target.write(json_string)
        target.close()
        print(time.ctime()+" finish write")

        # TODO ADD logging
    else:
        print("Get error response from traffic lib")
        print(json.dumps(result))

    t = threading.Timer(INTERVAL, timer)
    t.start()
    t.join()




def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc:  # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            print("error in mkdir")
            raise


def main():
    if len(sys.argv) < 3:
        print("Please provide two input parameter: the root directory for output and the API key")
        sys.exit(1)
    else:
	time.sleep(30)                   # sleep for 10 sec until all service are ready
        global DEST_ROOT, URL
        DEST_ROOT = sys.argv[1]
        URL = URL + sys.argv[2]
        print("Root dir is " + DEST_ROOT+", URL is " + URL)
        timer()
        # while True:
        #     time.sleep(1)

if __name__ == "__main__": main()

