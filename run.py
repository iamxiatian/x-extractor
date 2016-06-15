#!/usr/bin/python
import os, sys

def listfiles(path):
	"""
		list all files under given path, all paths are ignored.
	"""
	cur_files = os.listdir(path)
	all_files = []
	for file_name in cur_files:
		full_name = os.path.abspath(os.path.join(path, file_name))
		if os.path.isdir(full_name):
			next_level_files = listfiles(full_name)
			all_files.extend(next_level_files)
		else:
			all_files.append(full_name)
	return all_files

def get_full_class_name(class_name, search_dir):
	"""
	get full class name under path search_dir, if class_name is 'MyTest', 
	and there exists a class 'hello.MyTest', this method will return 
	'hello.MyTest'
	"""
	abspath = os.path.abspath(search_dir)
	all_files = listfiles(search_dir)

	#keep class name without ".class" extension name
	all_classes = [f[len(abspath)+1:len(f)-6].replace('/', '.') for f in all_files]

	#check equals
	for c in all_classes:
		if c==class_name:
			return c

	#check contains
	for c in all_classes:
		if c.lower().endswith('.' + class_name.lower()):
			return c

	return class_name

#loop directory and get all libraries
def getlibraries(HOME):
	split_char = ':'
	if os.name=='nt':
		split_char = ';'
	jars = "."
	
	jars = jars + split_char + HOME + "/my_conf"
	jars = jars + split_char + HOME + "/src/main/resources"
	jars = jars + split_char + HOME + "/src/main/java"
	jars = jars + split_char + HOME + "/build/classes/main"

	libdir = HOME + "/dist"
	for jar in os.listdir(libdir):
		if(jar==".svn"):continue
		fullname = os.path.join(libdir,jar)
		if os.path.isdir(fullname):
			for subjar in os.listdir(fullname):
				if subjar.endswith('.jar'):
					jars = jars + split_char + os.path.join(fullname, subjar)
		else:
			jars = jars + split_char + fullname
			
	return jars


home = os.getcwd()
if(os.path.basename(home)=='bin'):
	home = os.path.join(home,'..')

libpath = getlibraries(home)
command = 'java -Xmx8G -cp "' + libpath + '" '



if(len(sys.argv)==1):
	print "usage:./run.py ClassName Parameters"
else:
	args = sys.argv
	className =  get_full_class_name(args[1], './build/classes/main')
	command = command + ' ' + className
	for i in range(2,len(args)):
		command = command + ' "' + args[i] + '"'

	#print "execute ", command
	os.system(command)
