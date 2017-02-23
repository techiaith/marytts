#!/bin/python

import sys
import codecs
import MySQLdb

text_per_page = 1000

def load_file(mysql_host, mysql_user, mysql_password, mysql_db, locale, infile):

    connection = MySQLdb.Connect(host=mysql_host,
                                 user=mysql_user,
                                 passwd=mysql_password,
                                 db=mysql_db)

    cursor = connection.cursor()
    insertsql = "INSERT INTO " + locale + "_cleanText" + " (page_id, text_id, processed, cleanText) VALUES (%s, %s, %s, %s)"
    text_id = int(1)
    page_id = int(1)

    with codecs.open(infile, 'r', encoding='utf-8') as in_file:
        for line in in_file.readlines():
            print page_id, text_id, line.rstrip().encode('utf-8')
            cursor.execute(insertsql, (page_id, text_id, 0, line.rstrip().encode('utf-8')))
            connection.commit()
            if text_id % text_per_page == 0:
                page_id += 1

            text_id += 1

MySQLHost = sys.argv[1]
MySQLUser = sys.argv[2]
MySQLPassword = sys.argv[3]
MySQLDB = sys.argv[4]
locale = sys.argv[5]

infile = sys.argv[6]

load_file(MySQLHost, MySQLUser, MySQLPassword, MySQLDB, locale, infile)



