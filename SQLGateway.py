#!/usr/bin/python

import MySQLdb as mdb
import sys
import cgi
import cgitb
import os

#
# Good grief, we have to generate our own headers?  Crazy.
#
print 'Content-type: text/html\n\n'

form = cgi.FieldStorage()

con = None

if os.environ['REQUEST_METHOD'] == 'POST':
	print "Post request received."

	# Create SQL entry
	try: 
		print "Connecting to db..."
		con = mdb.connect('localhost', 'mhealth', 'mhealth', 'mhealthplay')
		print "Connected."
		print "Time: " + str(form.getvalue("time"))
		# NOTE: the query string puts the mac address and time
		# parameter in single quotes. Do NOT put the mac parameter 
		# in single quotes or the syntax will fail. 
		# TODO: sanitize data inputs to avoid injection
		queryString = "INSERT INTO masson_accel_table (" +\
		    "mac, time, energy" +\
		    ") VALUES (" +\
		    "'" + str(form.getvalue("mac")) + "', " +\
		    "'" + str(form.getvalue("time")) + "', " +\
		    str(form.getvalue("energy")) +\
		    ");"

		cur = con.cursor()
		print "Executing: " + queryString
		cur.execute(queryString)
		print "Executed."

	except mdb.Error, e:
		print "Error connecting or executing query string." 
		print e
	finally:
		if con:
			con.close()
			    
	
elif os.environ['REQUEST_METHOD'] == 'GET':
	print "<h3>Latest 60 data entries</h3>"
	try:
		con = mdb.connect('localhost', 'mhealth', 'mhealth', 'mhealthplay')
		cur = con.cursor()
		cur.execute("select * from masson_accel_table ORDER BY time DESC LIMIT 60")
		rows = cur.fetchall()

		# Display the data in a table
		print "<table border='1'>"
		print "<tr><th>mac</th><th>time</th><th>energy</th></tr>"
		for row in rows:
			print "<tr>"
			for col in row:
				print "<td>"
				print col
				print "</td>"

			print "</tr>"
		print "</table>"
	
	except mdb.Error, e:
		print "Error %d = %s<p>" % (e.args[0],e.args[1])
		sys.exit(1)
		
	finally:
		if con:
			con.close()
		
else:
	print "You sent a " + str(os.environ['REQUEST_METHOD']) + \
	    " request. This script only works with POST and GET requests."
