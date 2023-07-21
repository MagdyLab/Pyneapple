import jpype
import os
#hello_txt = os.path.join(os.path.dirname(__file__), 'example.txt')
jar_dir = os.path.join(os.path.dirname(__file__), 'pyneapple-0.1.0-SNAPSHOT-jar-with-dependencies.jar')
#with open(hello_txt,'r') as f:
#        print (f.read())
if not jpype.isJVMStarted():
        jpype.startJVM("-Xmx20480m", classpath = [jar_dir])
