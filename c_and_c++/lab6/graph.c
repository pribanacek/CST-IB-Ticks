#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "graph.h"

Node *empty = NULL;

Node *node(int value, Node *left, Node *right) {
  Node *r = malloc(sizeof(Node));
  r->marked = false;
  r->value = value;
  r->left = left;
  r->right = right;
  return r;
}


/* Basic Problems */

int size(Node *node) {
  if (node != NULL && !node->marked) {
    node->marked = true;
    return 1 + size(node->left) + size(node->right);
  }
  return 0;
}


void unmark(Node *node) {
  if (node != NULL && node->marked) {
    node->marked = false;
    unmark(node->left);
    unmark(node->right);
  }
}

bool path_from(Node *node1, Node *node2) {
  if (node1 == NULL || node2 == NULL || node1->marked) {
    return false;
  } else if (node1 == node2) {
    return true;
  } else {
    return path_from(node1->left, node2) || path_from(node1->right, node2);
  }
}


bool cyclic(Node *node) {
  if (node == NULL) {
    return false;
  } else {
    return path_from(node->left, node) || path_from(node->right, node);
  }
}


/* Challenge problems */

void get_nodes(Node *node, Node **dest) {
  if (node != NULL && !node->marked) {
    node->marked = true;
    dest[0] = node;
    dest++;
    get_nodes(node->left, dest);
    get_nodes(node->right, dest);
  }
}

void graph_free(Node *node) {
  int length = size(node);
  unmark(node);
  Node **buffer = malloc(sizeof(struct node) * length);
  get_nodes(node, buffer);
  for (int i = 0; i < length; i++) {
    free(buffer[i]);
  }
  free(buffer);
}
