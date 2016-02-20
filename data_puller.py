#!/usr/bin/python


import json
import urllib2
import os
import errno

import time, threading
from datetime import datetime
from threading import Thread

import sys

SL_FAULT_INFO_API = "SL_FAULT_INFO"
TRAFFIC_SITUATION_API = "TRAFFIC_SITUATION"
SL_FAULT_INFO_URL = 'http://api.sl.se/api2/deviations.json?key='
TRAFFIC_SITUATION_URL = 'http://api.sl.se/api2/trafficsituation.json?key='
DEST_ROOT = './'
INTERVAL = 5 * 60


def pull_sl_fault_info():

    result = json.load(urllib2.urlopen(SL_FAULT_INFO_URL))
    print(time.ctime() + " send " + SL_FAULT_INFO_URL)
    if result['StatusCode'] == 0:
        json_string = json.dumps(result)
        now = datetime.now()
        file_name = now.strftime('%y-%m-%d-%H-%M-%S.json')
        mypath = DEST_ROOT + "/" + SL_FAULT_INFO_API + "/" + str(now.year) + "/" + str(now.month) + "/" + str(now.day)
        mkdir_p(mypath)
        target = open(mypath + "/" + file_name, 'w')
        target.write(json_string)
        target.close()
        print(time.ctime()+" finish write")

        # TODO ADD logging
    else:
        print("Get error response from traffic lib")
        print(json.dumps(result))

    t = threading.Timer(INTERVAL, pull_sl_fault_info)
    t.start()


def pull_traffic_situation():
    result = json.load(urllib2.urlopen(TRAFFIC_SITUATION_URL))
    print(time.ctime() + " send " + TRAFFIC_SITUATION_URL)
    if result['StatusCode'] == 0:
        json_string = json.dumps(result)
        now = datetime.now()
        file_name = now.strftime('%y-%m-%d-%H-%M-%S.json')
        mypath = DEST_ROOT + "/" + TRAFFIC_SITUATION_API + "/" + str(now.year) + "/" + str(now.month) + "/" + str(now.day)
        mkdir_p(mypath)
        target = open(mypath + "/" + file_name, 'w')
        target.write(json_string)
        target.close()
        print(time.ctime()+" finish write")

        # TODO ADD logging
    else:
        print("Get error response from traffic lib")
        print(json.dumps(result))

    t = threading.Timer(INTERVAL, pull_traffic_situation)
    t.start()


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
    if len(sys.argv) < 4:
        print("Please provide two input parameter: the root directory for output and the API key(s)")
        sys.exit(1)
    else:
        global DEST_ROOT, SL_FAULT_INFO_URL, TRAFFIC_SITUATION_URL
        DEST_ROOT = sys.argv[1]
        SL_FAULT_INFO_URL = SL_FAULT_INFO_URL + sys.argv[2]
        TRAFFIC_SITUATION_URL = TRAFFIC_SITUATION_URL+ sys.argv[3]
        print("Root dir is " + DEST_ROOT + ", FAULT_INFO_URL is " + SL_FAULT_INFO_URL+", TRAFFIC_SITUATION_URL is " + TRAFFIC_SITUATION_URL)
        time.sleep(30)

        t1 = Thread(target=pull_sl_fault_info())
        t1.start()
        t2 = Thread(target=pull_traffic_situation())
        t2.start()

if __name__ == "__main__": main()

