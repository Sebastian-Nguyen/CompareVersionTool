# Project directory structure
- path/to/project_dir
	+ xyz_root (pom.xml)
	+ xyz_module_1 (pom.xml)
	+ xyz_module_2 (pom.xml)
	+ ...
	+ xyz_module_n (pom.xml)

# XYZ Version Comparison
This is the tool that automatically compares the version of each xyz module with its version in xyz_root and then save the result to a csv file.

# Installing
 
1, Sync 'develop' branch to local directory
 
2, Open terminal (cmd) at the directory
 
3, Create output folder

 ```
 rd -r out 2> NUL | mkdir out
 ```
	
 - or in powershell:

 ```
 rd -r out 2> $NUL ; mkdir out
 ```

4, Compile .java files
    
 ```
 javac -d out src/com/tool/form/*.java src/com/tool/model/*.java src/com/tool/util/*.java
 ```
 
5, Create jar file named "compare_tool.jar"

 ```
 jar -cvfe out\compare_tool.jar com.tool.form.MainForm -C out/ .
 ```

6, Run the jar file

 ```
 java -jar out\compare_tool.jar
 ```

# How to use the tool

1, Select or input the XYZ root directory

2, Click the `Check unmatched version & export to CSV` button

3, Save result to a CSV file
