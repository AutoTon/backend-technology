# 栈

```
public class Stack {

    public Node head;
    public Node current;

    //方法：入栈操作
    public void push(int data) {
        if (head == null) {
            head = new Node(data);
            current = head;
        } else {
            Node node = new Node(data);
            node.pre = current;//current结点将作为当前结点的前驱结点
            current = node;    //让current结点永远指向新添加的那个结点
        }
    }

    public Node pop() {
        if (current == null) {
            return null;
        }

        Node node = current; // current结点是我们要出栈的结点
        current = current.pre;  //每出栈一个结点后，current后退一位
        return node;

    }

    class Node {
        int data;
        Node pre;  //我们需要知道当前结点的前一个结点

        public Node(int data) {
            this.data = data;
        }
    }

    public static void main(String[] args) {

        Stack stack = new Stack();
        stack.push(1);
        stack.push(2);
        stack.push(3);

        System.out.println(stack.pop().data);
        System.out.println(stack.pop().data);
        System.out.println(stack.pop().data);
    }
}
```

# 队列

```
public class Queue {
    public Node head;
    public Node curent;

    //方法：链表中添加结点
    public void add(int data) {
        if (head == null) {
            head = new Node(data);
            curent = head;
        } else {
            curent.next = new Node(data);
            curent = curent.next;
        }
    }

    //方法：出队操作
    public int pop() throws Exception {
        if (head == null) {
            throw new Exception("队列为空");
        }

        Node node = head;  //node结点就是我们要出队的结点
        head = head.next; //出队之后，head指针向下移

        return node.data;

    }

    class Node {
        int data;
        Node next;

        public Node(int data) {
            this.data = data;
        }
    }

    public static void main(String[] args) throws Exception {
        Queue queue = new Queue();
        //入队操作
        for (int i = 0; i < 5; i++) {
            queue.add(i);
        }

        //出队操作
        System.out.println(queue.pop());
        System.out.println(queue.pop());
        System.out.println(queue.pop());

    }
}
```

# 排序算法

## 直接插入排序

```
/**
 * 每次将元素插入到前面已排序数组
 * @param array 未排序数组
 */
private static void insertSort(int[] array) {
    for (int i = 1; i < array.length; i++) {
        int temp = array[i];
        for (int j = i - 1; j >= 0; j--) {
            if (temp < array[j]) {
                array[j + 1] = array[j];
                if (j == 0) {
                    array[j] = temp;
                }
            } else {
                array[j + 1] = temp;
                break;
            }
        }
    }
}
```

## 二分插入排序

```
public static void binarySort(int[] array) {
    int i, j;
    int high, low, mid;
    int temp;
    for (i = 1; i < array.length; i++) {
        // 查找区上界
        low = 0;
        // 查找区下界
        high = i - 1;
        //将当前待插入记录保存在临时变量中
        temp = array[i];
        while (low <= high) {
            // 找出中间值
            // mid = (low + high) / 2;
            mid = (low + high) >> 1;
            //如果待插入记录比中间记录小
            if (temp < array[mid]) {
                // 插入点在低半区
                high = mid - 1;
            } else {
                // 插入点在高半区
                low = mid + 1;
            }
        }
        //将前面所有大于当前待插入记录的记录后移
        for (j = i - 1; j >= low; j--) {
            array[j + 1] = array[j];
        }
        //将待插入记录回填到正确位置.
        array[low] = temp;
    }
}
```

## 选择排序

```
/**
 * 每轮标记最小元素的下标，遍历找到未排序数组最小元素，与第一个元素进行交换
 * @param array 未排序数组
 */
private static void selectSort(int[] array) {
    for (int i = 0; i < array.length; i++) {
        int min = i;
        for (int j = i + 1; j < array.length; j++) {
            if (array[min] > array[j]) {
                min = j;
            }
        }
        if (min != i) {
            swap(array, min, i);
        }
    }
}
```

## 冒泡排序

```
private static void bubbleLowSort(int[] array) {
    for (int i = 0; i < array.length; i++) {
        for (int j = i + 1; j < array.length; j++) {
            if (array[i] > array[j]) {
                swap(array, i, j);
            }
        }
    }
}
```

```
/**
 * 相邻元素比较进行交换，每轮把最大值放到未排序数组最后面，若该轮中没有发生交换，则结束
 * @param array 未排序数组
 */
private static void bubbleSort(int[] array) {
    for (int i = 0; i < array.length; i++) {
        boolean hasSwap = false;
        for (int j = 0; j < array.length - i - 1; j++) {
            if (array[j] > array[j + 1]) {
                swap(array, j, j + 1);
                hasSwap = true;
            }
        }
        if (!hasSwap) {
            return;
        }
    }
}
```

## shell排序

```
private static void shellSort(int[] array) {
    int gap = array.length / 2;
    while (gap >= 1) {
        for(int i = gap; i < array.length; i++) {
            for(int j = i; j >= gap && array[j] < array[j-gap]; j -= gap) {
                swap(array, j, j - gap);
            }
        }
        gap = gap / 3;
    }
}
```

## 快速排序

```
private static void quickSort(int[] array) {
    quickSort(array, 0, array.length - 1);
}

private static void quickSort(int[] array, int left, int right) {
    if (left >= right) {
        return;
    }
    int i = left + 1;
    int j = right;
    int pivot = left;
    while (i < j) {
        while (i < j && array[pivot] > array[i]) {
            i++;
        }
        while (i < j && array[pivot] < array[j]) {
            j--;
        }
        if (i >= j) {
            break;
        }
        swap(array, i, j);
    }
    swap(array, j - 1, pivot);
    quickSort(array, left, i - 1);
    quickSort(array, i + 1, right);
}
```

## 归并排序

```
private static void mergeSort(int[] array) {
    mergeSort(array, 0, array.length - 1);
}

private static void mergeSort(int[] array, int left, int right) {
    if (left >= right) {
        return;
    }
    int middle = (left + right) / 2;
    mergeSort(array, left, middle);
    mergeSort(array, middle + 1, right);
    merge(array, left, middle, right);
}

private static void merge(int[] array, int left, int middle, int right) {
    int[] temp = new int[right - left + 1];
    int i = left;
    int j = middle + 1;
    int x = 0;
    while (i <= middle && j <= right) {
        if (array[i] < array[j]) {
            temp[x] = array[i];
            i++;
        } else {
            temp[x] = array[j];
            j++;
        }
        x++;
    }
    while (i <= middle) {
        temp[x] = array[i];
        x++;
        i++;
    }
    while (j <= right) {
        temp[x] = array[j];
        x++;
        j++;
    }
    for (int y = left; y <= right; y++) {
        array[y] = temp[y - left];
    }
}
```

## 堆排序

```
private static void heapSort(int[] array) {
    // 找到第一个非叶子结点
    for (int i = array.length / 2 - 1; i >= 0; i--) {
        adjustHeap(array, i, array.length);
    }
    for (int j = array.length - 1; j > 0; j--) {
        swap(array, 0, j);
        adjustHeap(array, 0, j);
    }
}

private static void adjustHeap(int[] array, int i, int length) {
    int temp = array[i];
    for (int k = 2 * i + 1; k < length; k = 2 * k + 1) {
        if (k + 1 < length && array[k] < array[k + 1]) {
            k++;
        }
        if (array[k] > temp) {
            array[i] = array[k];
            i = k;
        } else {
            break;
        }
    }
    array[i] = temp;
}
```

# 查找算法

## 二分查找

```
int binarysearch(int array[], int low, int high, int target) {
    if (low > high) return -1;
    int mid = low + (high - low) / 2;
    if (array[mid] > target)
        return binarysearch(array, low, mid - 1, target);
    if (array[mid] < target)
        return binarysearch(array, mid + 1, high, target);
    return mid;
}
```

## 算法题目

### 1000个无序的元素，最快的算法找出最大的10个元素

堆排序 > 冒泡排序 > 快速排序

### 循环递增数组，最快找出最小的元素

二分查找法。

```
public static  int findMin(int[] num, int begin, int end){
		
	 //当数组只有一个元素时，num[begin] == num[end] 直接返回
	 //当数组第一位小于最后一位时，第一位即为最小，因为数组循环递增 
	 if(num[begin] <= num[end]){
		 return num[begin];
	 }
	 //数组只有两位时，例如2,1这时候直接范围最后一位,如果是1,2则上边的If语句已经做出判断，这里不用考虑
	 if(end - begin == 1){
		 return num[end];
	 }
	 //算出中间位
	 int middle = (begin + end) / 2;
	 //如果中间位小于左边的，那么中间位便是最小值，因为数组循环递增
	 if(num[middle] < num[middle - 1]){
		 return num[middle];
	 }
	 //当中间位小于第一位时，中间位左边为严格递增，右边为循环递增，最小值一定在循环递增处
	 //反之，左边为循环递增，最小值一定在循环递增处
	 if(num[middle] > num[begin]){
		 return findMin(num,middle+1,end); 
	 }else{
		 return findMin(num,begin, middle-1);
	 }
		
}
```