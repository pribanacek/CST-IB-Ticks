#include <stdio.h>
#include <stdlib.h>
#include "list.h"

List *cons(int head, List *tail) {
  /* malloc() will be explained in the next lecture! */
  List *cell = malloc(sizeof(List));
  cell->head = head;
  cell->tail = tail;
  return cell;
}

/* Functions for you to implement */

int sum(List *list) {
  int x = list->head;
  if (list->tail == NULL) {
    return x;
  } else {
    return x + sum(list->tail);
  }
}

void iterate(int (*f)(int), List *list) {
  list->head = f(list->head);
  if (list->tail != NULL) {
    iterate(f, list->tail);
  }
}

void print(List *list) {
  printf("%d", list->head);
  if (list->tail != NULL) {
    printf(", ");
    print(list->tail);
  }
}

void print_list(List *list) {
  printf("[");
  print(list);
  printf("]\n");
}

/**** CHALLENGE PROBLEMS ****/

List *merge(List *list1, List *list2) {
  if (list1 == NULL) {
    return list2;
  }
  if (list2 == NULL) {
    return list1;
  }

  int a = list1->head;
  int b = list2->head;
  if (a < b) {
    List *tail = list1->tail;
    list1->tail = list2;
    return merge(list1, tail);
  } else {
    List *tail = list2->tail;
    list2->tail = list1;
    return merge(list2, tail);
  }
  return NULL;
}

void split(List *list, List **list1, List **list2) {
  if (list == NULL) {
    return;
  } else if (list->tail == NULL) {
    list1->head = list->head;
    return;
  }
  List *tail1 = list1;
  List *tail2 = list2;
  list1->head = list->head;
  list1->tail = tail1;
  list2->head = list->tail->head;
  list2->tail = tail2;
  split(list->tail->tail, list1, list2);
}

/* You get the mergesort implementation for free. But it won't
   work unless you implement merge() and split() first! */

List *mergesort(List *list) {
  if (list == NULL || list->tail == NULL) {
    return list;
  } else {
    List *list1;
    List *list2;
    split(list, &list1, &list2);
    list1 = mergesort(list1);
    list2 = mergesort(list2);
    return merge(list1, list2);
  }
}
