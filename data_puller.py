import json
import urllib2
import os
import errno

import time, threading
from datetime import datetime

KEY = ""
API_NAME = "SL_FAULT_INFO"
URL = 'http://api.sl.se/api2/deviations.json?key='+KEY
DEST_ROOT = './'
INTERVAL = 5 * 60


def timer():
    print(time.ctime())
    result = json.load(urllib2.urlopen(URL))

    if result['StatusCode'] == 0:
        json_string = json.dumps(result)
        now = datetime.now()
        file_name = now.strftime('%y-%m-%d-%H-%M-%S.json')
        mypath = DEST_ROOT + API_NAME + "/" + str(now.year) + "/" + str(now.month) + "/" + str(now.day)
        mkdir_p(mypath)
        target = open(mypath + "/" + file_name, 'w')
        target.write(json_string)
        target.close()
        # TODO ADD logging
    else:

    threading.Timer(INTERVAL, timer).start()


def mkdir_p(path):
    try:
        os.makedirs(path)
    except OSError as exc:  # Python >2.5
        if exc.errno == errno.EEXIST and os.path.isdir(path):
            pass
        else:
            raise


def main():
    timer()


if __name__ == "__main__": main()
