#/bin/sh
me=`whoami`
if [ $me == 'root' ]; then
  echo "不可使用 root 用户运行本脚本。"
  exit 1
fi

config_file="_app.config.js"

base_dir=`pwd`
target_file="dist.zip"
target_dir="dist"
tmp_dir="dist_tmp"
if [ -d ./$tmp_dir ];then
  rm -rf $tmp_dir
fi
#把目标文件复制到临时文件夹， 解压，然后在临时文件夹中删除目标文件（zip 文件）
mkdir $tmp_dir
cp $target_file ./$tmp_dir
cd ./$tmp_dir
unzip $target_file
rm -f ./$target_file

cd $base_dir
if [ -d $target_dir ];then
  rm -rf $target_dir
fi

if [ -f $config_file ];then
  cp $config_file $tmp_dir/$config_file
  echo "overwrite config file $config_file"
fi


mv $tmp_dir $target_dir
echo "=================> Updated new dist info for web site."