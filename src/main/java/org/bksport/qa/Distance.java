package org.bksport.qa;

// Chuong trinh nap vao bo nho danh sach cac tu vung trong CSDL
// so khop va sap xep theo thu tu tang dan.
// ??? Co phan biet chu hoa chu thuong? neu co thi phai bien doi ve chu thuong het truoc khi so khop

public class Distance {
  // Lay danh sach ten tu Allegrograph
  LabelList labelList = new LabelList();

  public Distance() {

  }

  int min(int a, int b, int c) {
    int min = a;
    if (min > b) {
      min = b;
    }
    if (min > c) {
      min = c;
    }
    return min;
  }

  int distance(String str1, String str2) {
    int m = str1.length();
    int n = str2.length();
    int[][] d = new int[m + 1][n + 1];

    // khoi tao
    for (int i = 0; i <= m; i++) {
      d[i][0] = i;
    }
    for (int j = 0; j <= n; j++) {
      d[0][j] = j;
    }

    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]
            + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
      }
    }
    return d[m][n];
  }

  // Places the elements of the given array into sorted order
  // using the merge sort algorithm.
  // post: array is in sorted (nondecreasing) order
  public void mergeSort(DistanceWeight[] array) {
    if (array.length > 1) {
      // split array into two halves
      DistanceWeight[] left = leftHalf(array);
      DistanceWeight[] right = rightHalf(array);

      // recursively sort the two halves
      mergeSort(left);
      mergeSort(right);

      // merge the sorted halves into a sorted whole
      merge(array, left, right);
    }
  }

  // Returns the first half of the given array.
  public DistanceWeight[] leftHalf(DistanceWeight[] array) {
    int size1 = array.length / 2;
    DistanceWeight[] left = new DistanceWeight[size1];
    for (int i = 0; i < size1; i++) {
      left[i] = array[i];
    }
    return left;
  }

  // Returns the second half of the given array.
  public DistanceWeight[] rightHalf(DistanceWeight[] array) {
    int size1 = array.length / 2;
    int size2 = array.length - size1;
    DistanceWeight[] right = new DistanceWeight[size2];
    for (int i = 0; i < size2; i++) {
      right[i] = array[i + size1];
    }
    return right;
  }

  public void merge(DistanceWeight[] result, DistanceWeight[] left,
      DistanceWeight[] right) {
    int i1 = 0; // index into left array
    int i2 = 0; // index into right array

    for (int i = 0; i < result.length; i++) {
      if (i2 >= right.length
          || (i1 < left.length && left[i1].getDis() <= right[i2].getDis())) {
        result[i] = left[i1]; // take from left
        i1++;
      } else {
        result[i] = right[i2]; // take from right
        i2++;
      }
    }
  }

  public String find(String userString) throws Exception {

    // khai bao mang MyClass
    DistanceWeight[] arrClass = new DistanceWeight[labelList.getCount()];

    for (int i = 0; i < labelList.getCount(); i++) {
      arrClass[i] = new DistanceWeight();
      arrClass[i].setStr(labelList.getListString()[i]);
      arrClass[i].setUri(labelList.getListUri()[i]);
      arrClass[i].setDis(distance(labelList.getListString()[i], userString));
    }

    /* Tien hanh sap xep giam dan */
    mergeSort(arrClass);

    /* Xuat danh sach cac tu goi y */
    for (int i = 0; i < arrClass.length; i++) {
      // System.out.print(arrClass[i].getStr() + "\t" + arrClass[i].getDis() +
      // "\n");
      return arrClass[i].getUri();
    }
    return null;
  }

  /***
   * end MergeSort
   * 
   * @throws Exception
   ***/

  public static void main(String[] args) throws Exception {
    // Lay danh sach ten tu Allegrograph
    Distance dis = new Distance();
    System.out.println(dis.find("Selena William"));
  }
  /*** end Main ***/
}
