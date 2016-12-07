from random import choice
from time import time

# A comparison between 2 AllSets funcitons written with my friend (Yoni Learner)
def AllSets(S,k):
#requires: S is a set, k >= 0
#effects: returns the list of all sets of size k with elements from S
    if k == 0:
        return [set()]
    elif S == set():
        return []
    elif k > 0:
        return_list = []
        for x in S:
            for p in AllSets(S - {x} ,k-1):
                if p | set({x}) not in return_list:
                    return_list += [p | set({x})]
        return return_list
def AllSets2(S,k):
#requires: S is a set, k >= 0
#effects: returns the list of all sets of size k with elements from S
     if k == 0:
        return [set()]
     elif S == set():
        return []
     elif k > 0:
        x = S.pop()
        S |= set({x})
        return AllSets2(S - {x}, k) + [y | set({x}) for y in AllSets2(S - {x}, k - 1)]


s1 = [1,2,3,4]
print AllSets(set(s1), 3)
print AllSets2(set(s1), 3)




t1 = (time())
for i in range(10000):
    AllSets(set(s1), 3)
print (time()) - t1


t2 = (time())
for i in range(10000):
    AllSets2(set(s1), 3)
print (time()) - t2
