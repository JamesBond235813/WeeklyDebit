#/bin/sh
me=`whoami`
if [ $me == 'root' ]; then
  echo "不可使用 root 用户运行本脚本。"
  exit 1
fi
base_dir=`pwd`
log_dir=${base_dir}/logs
app_name=silver-union-ops
profile="local"


PID=`ps -ef | grep ${app_name}.jar |grep "active=${profile}" | grep -v grep | awk '{print $2}'`;
if [ ${PID}x != x ];then
    echo "Shutting down ${app_name} PID[$PID]..."
    kill $PID ;
fi

for i in {1..10}; do
  PID=`ps -ef | grep ${app_name}.jar |grep "active=${profile}" | grep -v grep | awk '{print $2}'`;
  if [ ${PID}x != x ]; then
    echo "Waiting ${i} seconds..."
    sleep 1
  else
    break
  fi
done

PID=`ps -ef | grep ${app_name}.jar |grep "active=${profile}" | grep -v grep | awk '{print $2}'`;
if [ ${PID}x != x ];then
    echo "Shutting down force ${app_name} PID[$PID]..."
    kill -9 $PID ;
fi

# 4C8G jdk17 配置参数
#内存
#JP=" -Xms1G -Xmx2G -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=512M"
JP=" -Xms256m -Xmx1024m -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=256M"
#GC
# 目标最大 GC 停顿时间（按需调整）
JP="${JP} -XX:MaxGCPauseMillis=200"
# 设置 Region 大小（大对象多的场景可增大）
JP="${JP} -XX:G1HeapRegionSize=4m"
# G1 触发并发周期的堆占用阈值
JP="${JP} -XX:InitiatingHeapOccupancyPercent=45"
# 内存溢出诊断:OOM 时生成堆转储
JP="${JP} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${log_dir}/"
# GC 日志 ${log_dir} 自动轮转保留最近 10 个文件，单个文件最大 100MB。
JP="${JP} -Xlog:gc*=info:file=${log_dir}/gc.log:time,uptime,level,tags:filecount=10,filesize=100m"


nohup java -jar ${app_name}.jar  $JP  --spring.profiles.active=${profile} > /dev/null &
echo "正在启动 ${app_name} ..."
sleep 1
tail -40f logs/${app_name}.log
