#include <stdlib.h>
#include "tree.h"

Tree *empty = NULL;

/* BASE EXERCISE */

int tree_member(int x, Tree *tree) {
  if (tree == NULL) {
    return 0;
  } else if (x == tree->value) {
    return 1;
  } else {
    return tree_member(x, tree->left) || tree_member(x, tree->right);
  }
}

Tree *tree_insert(int x, Tree *tree) {
  if (tree == empty) {
    Tree *leaf = malloc(sizeof(Tree));
    leaf->value = x;
    leaf->left = empty;
    leaf->right = empty;
    return leaf;
  } else if (x == tree->value) {
    return tree;
  } else if (x > tree->value) {
    tree->right = tree_insert(x, tree->right);
    return tree;
  } else { //x < tree->value
    tree->left = tree_insert(x, tree->left);
    return tree;
  }
  return empty;
}

void tree_free(Tree *tree) {
  if (tree != empty) {
    if (tree->left != empty) {
      tree_free(tree->left);
    }
    if (tree->right != empty) {
      tree_free(tree->right);
    }
    free(tree);
  }
}

/* CHALLENGE EXERCISE */

void pop_minimum(Tree *tree, int *min, Tree **new_tree) {
  if (tree->left == empty) {
    *min = tree->value;
    *new_tree = tree->right;
  } else {
    pop_minimum(tree->left, *min, *new_tree->left);
  }
}

Tree *tree_remove(int x, Tree *tree) {
  if (tree->value == x) {

  }
  return empty;
}
