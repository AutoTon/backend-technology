echo 
echo

pid=$1
if [ ! $pid ]  
then
  echo �������idΪ��
  exit
fi
echo �������id = $pid


echo -e "\033[33m--------------------------------���㸺��------------------------------------------\033[0m"
echo -e "\033[34m[��3�����CPUʹ����]\033[0m"

declare -i counter=0
#use while to loop
while ((counter<3));do
    let ++counter
    echo $(top -b -n 1 -p $pid  2>&1 | awk -v pid=$pid '{if ($1 == pid)print $9}')
    sleep 1
done
echo 

echo -e "\033[34m[�����߳���]\033[0m"
ps hH p $pid|wc -l
echo

# lsof ��װ����yum install lsof -y
echo -e "\033[33m--------------------------------�洢����------------------------------------------\033[0m"
echo -e "\033[34m[�����ڴ�ռ�ʹ��]\033[0m"
echo $(cat  /proc/$pid/status|grep -e VmRSS| awk '{print $2/1024 "Mb"}')
echo 


echo -e "\033[34m[���̴��ļ������]\033[0m"
echo $(lsof -n|awk '{print $2}'|grep -e $pid|uniq -c |awk '{print $1}')
echo

echo -e "\033[33m--------------------------------���縺��------------------------------------------\033[0m"
echo -e "\033[34m[Ӧ��TCP������]\033[0m"
lsof -p $pid -nP|grep TCP|wc -l
echo

echo -e "\033[34m[�˿�TCP����״̬]\033[0m"
echo LISTEN $(lsof -p $pid -nP |grep TCP|grep LISTEN|wc -l)
echo CLOSE_WAIT $(lsof -p $pid -nP |grep TCP|grep CLOSE_WAIT|wc -l)
echo ESTABLISHED $(lsof -p $pid -nP |grep TCP|grep ESTABLISHED|wc -l)
echo TIME_WAIT $(lsof -p $pid -nP |grep TCP|grep TIME_WAIT|wc -l)
echo -e "\033[32mLISTEN-����״̬\033[0m"
echo -e "\033[32mCLOSE_WAIT-�Է������ر����ӻ��������쳣���������ж�-״̬\033[0m"
echo -e "\033[32mESTABLISHED-�������ݴ���״̬\033[0m"
echo -e "\033[32mTIME_WAIT-������ϵȴ���ʱ����״̬\033[0m"
echo
echo

echo
echo -e "\033[33m--------------------------------�ο�------------------------------------------\033[0m"


echo -e "\033[32m jstack �����ڴ�ӡ�̶߳�ջ��Ϣ��ע���ע�߳�����״��\033[0m"