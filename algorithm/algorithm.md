# 数据结构

## 栈

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

## 队列

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

## 跳表

链表加多级索引的结构。（空间换时间）

![](images/jump-list-demo.jpg)

### 实际应用

+ Redis中的有序集合（Sorted Set）。

## 散列表

基于数组实现。

### 散列冲突解决

#### 开放寻址

适用于数据量比较小、装载因子小的场景。Java的ThreadLocalMap采用了该算法。

+ 线性探测
+ 二次探测
+ 双重散列

#### 链表法

需要额外的空间存储链表。Java的HashMap采用了该算法。

### 实际应用

+ Word文档中单词拼写检查功能。

### 散列函数的设计

+ 不能太复杂。否则消耗太多计算资源，影响性能。
+ 散列函数生成的值要尽可能随机并且均匀分布。

### 扩容

大部分情况下，动态扩容的散列表插入一个数据都很快，但是在特殊情况下，当装载因子已经到达阈值，需要先进行扩容，再插入数据。这个时候，插入数据就会变得很慢，甚至会无法接受。

将扩容操作穿插在插入操作的过程中，分批完成。当装载因子触达阈值之后，我们只申请新空间，但并不将老的数据搬移到新散列表中。当有新数据要插入时，我们将新数据插入新散列表中，并且从老的散列表中拿出一个数据放入到新散列表。对于查询操作，为了兼容了新、老散列表中的数据，我们先从新散列表中查找，如果没有找到，再去老的散列表中查找。

## 树

+ 节点的高度：节点到叶子节点的最长距离。（边数）
+ 节点的深度：根节点到这个节点的边数。
+ 节点的层数：节点的深度+1。
+ 树的高度：根节点的高度。

### 二叉树

#### 遍历

+ 前序遍历：先打印这个节点，然后再打印它的左子树，最后打印它的右子树。
+ 中序遍历：先打印左子树，然后再打印节点本身，最后打印它的右子树。
+ 后序遍历：先打印左子树，然后再打印右子树，最后打印节点本身。

#### 二叉查找树

在树中的任意一个节点，其左子树中的每个节点的值，都要小于这个节点的值，而右子树节点的值都大于这个节点的值。

中序遍历二叉查找树，可以输出有序的数据序列，时间复杂度是O(n)，非常高效。

##### 查找算法

```
public class BinarySearchTree {
  private Node tree;

  public Node find(int data) {
    Node p = tree;
    while (p != null) {
      if (data < p.data) p = p.left;
      else if (data > p.data) p = p.right;
      else return p;
    }
    return null;
  }

  public static class Node {
    private int data;
    private Node left;
    private Node right;

    public Node(int data) {
      this.data = data;
    }
  }
}
```

##### 插入算法

```
public void insert(int data) {
  if (tree == null) {
    tree = new Node(data);
    return;
  }

  Node p = tree;
  while (p != null) {
    if (data > p.data) {
      if (p.right == null) {
        p.right = new Node(data);
        return;
      }
      p = p.right;
    } else { // data < p.data
      if (p.left == null) {
        p.left = new Node(data);
        return;
      }
      p = p.left;
    }
  }
}
```

##### 删除算法

```
public void delete(int data) {
  Node p = tree; // p指向要删除的节点，初始化指向根节点
  Node pp = null; // pp记录的是p的父节点
  while (p != null && p.data != data) {
    pp = p;
    if (data > p.data) p = p.right;
    else p = p.left;
  }
  if (p == null) return; // 没有找到

  // 要删除的节点有两个子节点
  if (p.left != null && p.right != null) { // 查找右子树中最小节点
    Node minP = p.right;
    Node minPP = p; // minPP表示minP的父节点
    while (minP.left != null) {
      minPP = minP;
      minP = minP.left;
    }
    p.data = minP.data; // 将minP的数据替换到p中
    p = minP; // 下面就变成了删除minP了
    pp = minPP;
  }

  // 删除节点是叶子节点或者仅有一个子节点
  Node child; // p的子节点
  if (p.left != null) child = p.left;
  else if (p.right != null) child = p.right;
  else child = null;

  if (pp == null) tree = child; // 删除的是根节点
  else if (pp.left == p) pp.left = child;
  else pp.right = child;
}
```

#### 平衡二叉树

二叉树中任意一个节点的左右子树的高度相差不能大于1。

#### 红黑树

+ 根节点是黑色的。
+ 每个叶子节点都是黑色的空节点（NIL），也就是说，叶子节点不存储数据。
+ 任何相邻的节点都不能同时为红色，也就是说，红色节点是被黑色节点隔开的。
+ 每个节点，从该节点到达其可达叶子节点的所有路径，都包含相同数目的黑色节点。

# 排序算法

## 稳定性

排序前后相同值的元素顺序不变。

## 直接插入排序（稳定）

优先于冒泡排序，原因是移动较少。

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

## 选择排序（不稳定）

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

## 冒泡排序（稳定）

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

## 快速排序（不稳定）

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

### 优化

#### 三数取中法

从区间的首、尾、中间，分别取出一个数，然后对比大小，取这3个数的中间值作为分区点。

#### 随机法

每次从要排序的区间中，随机选择一个元素作为分区点。

## 归并排序（稳定）

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

## 桶排序

将要排序的数据分到几个有序的桶里，每个桶里的数据再单独进行排序。桶内排完序之后，再把每个桶里的数据按照顺序依次取出，组成的序列就是有序的了。

### 对要排序数据的要求

+ 要排序的数据需要很容易就能划分成m个桶，并且，桶与桶之间有着天然的大小顺序。这样每个桶内的数据都排序完之后，桶与桶之间的数据不需要再进行排序。
+ 数据在各个桶之间的分布是比较均匀的。

### 适用场景

桶排序比较适合用在外部排序中。所谓的外部排序就是数据存储在外部磁盘中，数据量比较大，内存有限，无法将数据全部加载到内存中。

## 计数排序

计数排序其实是桶排序的一种特殊情况。当要排序的n个数据，所处的范围并不大的时候，比如最大值是 k，我们就可以把数据划分成k个桶。每个桶内的数据值都是相同的，省掉了桶内排序的时间。

```
// 计数排序，a是数组，n是数组大小。假设数组中存储的都是非负整数。
public void countingSort(int[] a, int n) {
  if (n <= 1) return;

  // 查找数组中数据的范围
  int max = a[0];
  for (int i = 1; i < n; ++i) {
    if (max < a[i]) {
      max = a[i];
    }
  }

  int[] c = new int[max + 1]; // 申请一个计数数组c，下标大小[0,max]
  for (int i = 0; i <= max; ++i) {
    c[i] = 0;
  }

  // 计算每个元素的个数，放入c中
  for (int i = 0; i < n; ++i) {
    c[a[i]]++;
  }

  // 依次累加
  for (int i = 1; i <= max; ++i) {
    c[i] = c[i-1] + c[i];
  }

  // 临时数组r，存储排序之后的结果
  int[] r = new int[n];
  // 计算排序的关键步骤，有点难理解
  for (int i = n - 1; i >= 0; --i) {
    int index = c[a[i]]-1;
    r[index] = a[i];
    c[a[i]]--;
  }

  // 将结果拷贝给a数组
  for (int i = 0; i < n; ++i) {
    a[i] = r[i];
  }
}
```

### 适用场景

+ 只能用在数据范围不大的场景中。
+ 只能给非负整数排序，如果要排序的数据是其他类型的，要将其在不改变相对大小的情况下，转化为非负整数。

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

```
public int bsearch(int[] a, int n, int value) {
  int low = 0;
  int high = n - 1;

  while (low <= high) {
    int mid = (low + high) / 2; // 注意这里low+hign可能溢出，最好改成low+((high-low)>>1)
    if (a[mid] == value) {
      return mid;
    } else if (a[mid] < value) {
      low = mid + 1;
    } else {
      high = mid - 1;
    }
  }

  return -1;
}
```

### 适用场景

+ 依赖的是顺序表结构，简单点说就是数组。
+ 针对的是有序数据。
+ 数据量太小不适合二分查找。

### 进阶

#### 查找第一个值等于给定值的元素

```
public int bsearch(int[] a, int n, int value) {
  int low = 0;
  int high = n - 1;
  while (low <= high) {
    int mid =  low + ((high - low) >> 1);
    if (a[mid] > value) {
      high = mid - 1;
    } else if (a[mid] < value) {
      low = mid + 1;
    } else {
      if ((mid == 0) || (a[mid - 1] != value)) return mid;
      else high = mid - 1;
    }
  }
  return -1;
}
```

#### 查找最后一个值等于给定值的元素

```
public int bsearch(int[] a, int n, int value) {
  int low = 0;
  int high = n - 1;
  while (low <= high) {
    int mid =  low + ((high - low) >> 1);
    if (a[mid] > value) {
      high = mid - 1;
    } else if (a[mid] < value) {
      low = mid + 1;
    } else {
      if ((mid == n - 1) || (a[mid + 1] != value)) return mid;
      else low = mid + 1;
    }
  }
  return -1;
}
```

#### 查找第一个大于等于给定值的元素

```
public int bsearch(int[] a, int n, int value) {
  int low = 0;
  int high = n - 1;
  while (low <= high) {
    int mid =  low + ((high - low) >> 1);
    if (a[mid] >= value) {
      if ((mid == 0) || (a[mid - 1] < value)) return mid;
      else high = mid - 1;
    } else {
      low = mid + 1;
    }
  }
  return -1;
}
```

#### 查找最后一个小于等于给定值的元素

```
public int bsearch7(int[] a, int n, int value) {
  int low = 0;
  int high = n - 1;
  while (low <= high) {
    int mid =  low + ((high - low) >> 1);
    if (a[mid] > value) {
      high = mid - 1;
    } else {
      if ((mid == n - 1) || (a[mid + 1] > value)) return mid;
      else low = mid + 1;
    }
  }
  return -1;
}
```

## 哈希算法

将任意长度的二进制值串映射为固定长度的二进制值串。

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