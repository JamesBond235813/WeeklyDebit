#/bin/sh
me=`whoami`
if [ $me == 'root' ]; then
  echo "不可使用 root 用户运行本脚本。"
  exit 1
fi
base_dir="/Users/littej/data_receiver/sms"
log_dir=${base_dir}/logs
app_name=get_sms_repley



PID=`ps -ef | grep python | grep ${app_name} | grep -v grep | awk '{print $2}'`;
if [ ${PID}x != x ];then
    echo "Shutting down ${app_name} PID[$PID]..."
        kill $PID ;
        echo "Waiting 5 seconds..."
        sleep 5
fi

nohup python -u ${base_dir}/${app_name}.py >> ${log_dir}/${app_name}.log 2>&1 &
echo "正在启动 ${app_name} ..."
sleep 1
tail -40f ${log_dir}/${app_name}.log