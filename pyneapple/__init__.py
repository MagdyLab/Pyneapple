import jpype
import os
hello_txt = os.path.join(os.path.dirname(__file__), 'example.txt')
jar_dir = os.path.join(os.path.dirname(__file__), 'pyneappleNew.jar')
with open(hello_txt,'r') as f:
        print (f.read())
print(jar_dir)
if not jpype.isJVMStarted():
        jpype.startJVM("-Xmx20480m", classpath = [jar_dir])
