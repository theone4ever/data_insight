#!/usr/bin/python


import json
import urllib2
import os
import errno

import time, threading
from datetime import datetime
from threading import Thread

import sys
import httplib

SL_FAULT_INFO_API = "SL_FAULT_INFO"
TRAFFIC_SITUATION_API = "TRAFFIC_SITUATION"
ROAD_ACCIDENT_API = "ROAD_ACCIDENT"
SL_FAULT_INFO_URL = 'http://api1.sl.se/api2/deviations.json?key='
TRAFFIC_SITUATION_URL = 'http://api1.sl.se/api2/trafficsituation.json?key='
DEST_ROOT = './'
INTERVAL = 5 * 60

data1 = '<REQUEST>\
                <LOGIN authenticationkey="'
data2 = '" /> \
                <QUERY objecttype="Situation">                                  \
                    <FILTER>                                                         \
                        <EQ name="Deviation.MessageType" value="Olycka" />                \
                    </FILTER>                                                          \
                    <INCLUDE>Deviation.Id</INCLUDE>                                     \
                    <INCLUDE>Deviation.Header</INCLUDE>                                  \
                    <INCLUDE>Deviation.IconId</INCLUDE>                                   \
                    <INCLUDE>Deviation.Geometry.WGS84</INCLUDE>                            \
                </QUERY>                                                                \
            </REQUEST> '
data = ''

def pull_sl_fault_info():
    try:
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
            print(time.ctime() + " finish write")

            # TODO ADD logging
        else:
            print("Get error response from traffic lib")
            print(json.dumps(result))
    except urllib2.HTTPError, e:
        print('Error in pull_sl_fault_info, HTTPError = ' + str(e.code))
    except urllib2.URLError, e:
        print('Error in pull_sl_fault_info, URLError = ' + str(e.reason))
    except httplib.HTTPException, e:
        print('Error in pull_sl_fault_info, HTTPException')
    except Exception:
        import traceback
        print('Error in pull_sl_fault_info, generic exception: ' + traceback.format_exc())

    t = threading.Timer(INTERVAL, pull_sl_fault_info)
    t.start()


def pull_traffic_situation():
    try:
        result = json.load(urllib2.urlopen(TRAFFIC_SITUATION_URL))
        print(time.ctime() + " send " + TRAFFIC_SITUATION_URL)
        if result['StatusCode'] == 0:
            json_string = json.dumps(result)
            now = datetime.now()
            file_name = now.strftime('%y-%m-%d-%H-%M-%S.json')
            mypath = DEST_ROOT + "/" + TRAFFIC_SITUATION_API + "/" + str(now.year) + "/" + str(now.month) + "/" + str(
                now.day)
            mkdir_p(mypath)
            target = open(mypath + "/" + file_name, 'w')
            target.write(json_string)
            target.close()
            print(time.ctime() + " finish write")

            # TODO ADD logging
        else:
            print("Get error response from traffic lib")
            print(json.dumps(result))
    except urllib2.HTTPError, e:
        print('Error in pull_traffic_situation, HTTPError = ' + str(e.code))
    except urllib2.URLError, e:
        print('Error in pull_traffic_situation, URLError = ' + str(e.reason))
    except httplib.HTTPException, e:
        print('Error in pull_traffic_situation, HTTPException')
    except Exception:
        import traceback
        print('Error in pull_traffic_situation, generic exception: ' + traceback.format_exc())

    t = threading.Timer(INTERVAL, pull_traffic_situation)
    t.start()


def pull_road_accident():
    try:
        req = urllib2.Request('http://api.trafikinfo.trafikverket.se/v1.1/data.json')
        req.add_header('Content-Type', 'text/xml')
        result = json.load(urllib2.urlopen(req, data))
        print(time.ctime() + " send " + ROAD_ACCIDENT_API)
        json_string = json.dumps(result)
        now = datetime.now()
        file_name = now.strftime('%y-%m-%d-%H-%M-%S.json')
        mypath = DEST_ROOT + "/" + ROAD_ACCIDENT_API + "/" + str(now.year) + "/" + str(now.month) + "/" + str(
            now.day)
        mkdir_p(mypath)
        target = open(mypath + "/" + file_name, 'w')
        target.write(json_string)
        target.close()
        print(time.ctime() + " finish write")
    except urllib2.HTTPError, e:
        print('Error in pull_road_accident, HTTPError = ' + str(e.code))
    except urllib2.URLError, e:
        print('Error in pull_road_accident, URLError = ' + str(e.reason))
    except httplib.HTTPException, e:
        print('Error in pull_road_accident, HTTPException')
    except Exception:
        import traceback
        print('Error in pull_road_accident, generic exception: ' + traceback.format_exc())

    # TODO ADD logging
    t = threading.Timer(INTERVAL, pull_road_accident)
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
    if len(sys.argv) < 5:
        print("Please provide three input parameter: the root directory for output and the API key(s)")
        sys.exit(1)
    else:
        global DEST_ROOT, SL_FAULT_INFO_URL, TRAFFIC_SITUATION_URL,data
        DEST_ROOT = sys.argv[1]
        SL_FAULT_INFO_URL = SL_FAULT_INFO_URL + sys.argv[2]
        TRAFFIC_SITUATION_URL = TRAFFIC_SITUATION_URL + sys.argv[3]
        data = data1+sys.argv[4]+data2
        print("Root dir is " + DEST_ROOT + ", FAULT_INFO_URL is " + SL_FAULT_INFO_URL + ", TRAFFIC_SITUATION_URL is " + TRAFFIC_SITUATION_URL)
        time.sleep(30)

        t1 = Thread(target=pull_sl_fault_info())
        t1.start()
        t2 = Thread(target=pull_traffic_situation())
        t2.start()
        t3 = Thread(target=pull_road_accident())
        t3.start()


if __name__ == "__main__": main()
