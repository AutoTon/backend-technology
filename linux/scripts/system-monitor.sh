echo 
echo 
echo -e "\033[33m--------------------------------���㸺��------------------------------------------\033[0m"

echo -e "\033[34m[CPU]\033[0m"
iostat -c | awk 'NR==3 || NR==4 {print}'
echo -e "\033[32muser-�û��ռ�CPUʱ��ռ�ȣ�system-ϵͳ�ռ�CPUʱ��ռ��\033[0m"
echo -e "\033[32miowait-CPU io�ȴ�ʱ��ռ�ȣ�idle-CPU����ʱ��ռ��\033[0m"

echo 
echo -e "\033[34m[������]\033[0m"
ps -ef|wc -l


echo 
 

echo -e "\033[33m--------------------------------�洢����------------------------------------------\033[0m"
echo -e "\033[34m[�ڴ�ռ�ʹ��]\033[0m"
free -m
echo -e "\033[32m��λ-Mb��Mem-�����ڴ棬Swap-�����ڴ�\033[0m"
echo -e "\033[32m��λ-Mb��total-���ڴ棬available-�����ڴ�\033[0m"
echo 
echo -e "\033[34m[���̿ռ�ʹ��]\033[0m"
df -h

echo 
echo -e "\033[34m[����������]\033[0m"
iostat -d -m | awk 'NR>2{print}'| awk '/./ {print}' 
echo -e "\033[32m��λ-Mb��tps-ÿ��io������MB_read/s-ÿ�������������MB_wrtn/s-ÿ��д��������\033[0m"
echo

echo -e "\033[34m[�ļ������]\033[0m"
lsof|awk '{print $2}'|wc -l
echo

echo -e "\033[33m--------------------------------���縺��------------------------------------------\033[0m"
echo -e "\033[34m[TCP������]\033[0m"
echo $(netstat -nat|wc -l)
echo

echo -e "\033[34m[�˿�����״̬]\033[0m"
netstat -na | awk '/^tcp/ {++S[$NF]} END {for(a in S) print a, S[a]}'
echo -e "\033[32mLISTEN-����״̬\033[0m"
echo -e "\033[32mCLOSE_WAIT-�Է������ر����ӻ��������쳣���������ж�-״̬\033[0m"
echo -e "\033[32mESTABLISHED-�������ݴ���״̬\033[0m"
echo -e "\033[32mTIME_WAIT-������ϵȴ���ʱ����״̬\033[0m"
echo

echo -e "\033[34m[��������]\033[0m"
sar -n DEV 1 3 |awk 'NR>2 {print}'
echo -e "\033[32mrxpck/s-ÿ����հ���������txpck/s-ÿ�뷢���İ�������\033[0m"
echo -e "\033[32mrxKB/s-ÿ����յ���������txKB/s-ÿ�뷢��������������λKB\033[0m"

echo
echo -e "\033[33m--------------------------------�ο�------------------------------------------\033[0m"
echo -e "\033[32mCPUռ������ǰ10�����̣�ps auxw|head -1;ps auxw|sort -rn -k3|head -10\033[0m"
echo -e "\033[32m�ڴ���������ǰ10�����̣�ps auxw|head -1;ps auxw|sort -rn -k4|head -10\033[0m"
echo -e "\033[32m�����ڴ�ʹ������ǰ10�����̣�ps auxw|head -1;ps auxw|sort -rn -k5|head -10\033[0m"