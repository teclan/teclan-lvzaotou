ps -afux|grep teclan-lvzaotou|awk '{print $2}'|xargs kill -9
