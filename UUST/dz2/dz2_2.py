import numpy as np
import math


b_i = np.array([22.2, 1.7, 42.7])
n = np.array([5.0, -5.0, -2.0])
alpha_deg = 49
alpha_rad = math.radians(alpha_deg)

n_magnitude = np.linalg.norm(n)
# 1.
print("\n")
print(f"|n| = {n_magnitude:.3f}")

n_normalized = n / n_magnitude
# 2.
print("\n")
print(f"n = {n_normalized[0]:.3f}i + {n_normalized[1]:.3f}j + {n_normalized[2]:.3f}k")

q1 = math.cos(alpha_rad/2)
q2 = n_normalized[0] * math.sin(alpha_rad/2)
q3 = n_normalized[1] * math.sin(alpha_rad/2)
q4 = n_normalized[2] * math.sin(alpha_rad/2)

q = np.array([q1, q2, q3, q4])
q_magnitude = np.linalg.norm(q)
q_normalized = q / q_magnitude
# 3.
print("\n")
print(f"q = {q_normalized[0]:.3f} + {q_normalized[1]:.3f}i + {q_normalized[2]:.3f}j + {q_normalized[3]:.3f}k")

q_magnitude = np.linalg.norm(q)
# 4.
print("\n")
print(f"|q| = {q_magnitude:.3f}")

r11 = q1*q1 + q2*q2 - q3*q3 - q4*q4
r12 = 2 * (q2*q3 + q1*q4)
r13 = 2 * (q2*q4 - q1*q3)
r21 = 2 * (q2*q3 - q1*q4)
r22 = q1*q1 - q2*q2 + q3*q3 - q4*q4
r23 = 2 * (q3*q4 + q1*q2)
r31 = 2 * (q2*q4 + q1*q3)
r32 = 2 * (q3*q4 - q1*q2)
r33 = q1*q1 - q2*q2 - q3*q3 + q4*q4

psi = math.atan2(r12, r11)
theta = math.asin(-r13)
phi = math.atan2(r23, r33)
psi_deg = math.degrees(psi)
theta_deg = math.degrees(theta)
phi_deg = math.degrees(phi)
# 5.
print("\n")
print(f"psi = {psi_deg:.3f} deg")
print(f"theta = {theta_deg:.3f} deg")
print(f"phi = {phi_deg:.3f} deg")

R_i_to_b = np.array([
    [r11, r12, r13],
    [r21, r22, r23],
    [r31, r32, r33]])
# 6.
print("\n")
print(f"r11 = {r11:.3f}")
print(f"r12 = {r12:.3f}")
print(f"r13 = {r13:.3f}")
print(f"r21 = {r21:.3f}")
print(f"r22 = {r22:.3f}")
print(f"r23 = {r23:.3f}")
print(f"r31 = {r31:.3f}")
print(f"r32 = {r32:.3f}")
print(f"r33 = {r33:.3f}")

b_b = np.dot(R_i_to_b, b_i)
# 7.
print("\n")
print(f"b_b = {b_b[0]:.3f}i + {b_b[1]:.3f}j + {b_b[2]:.3f}k")

b_i_magnitude = np.linalg.norm(b_i)
b_b_magnitude = np.linalg.norm(b_b)
# 8.
print("\n")
print(f"|b_i| = {b_i_magnitude:.3f}")
print(f"|b_b| = {b_b_magnitude:.3f}")

#by Spike433

#question 2


###### change this values
a = 10 #cm
l = 117 #m
r = 0.17 #mm
I = -173 #mA

a = a / 100
r = r/1000
I = I / 1000

ro = 1.68 / np.power(10,8)

#3.
B = 37 #mikroT
B = B / np.power(10,6)

#4.
m = 1 # kg

# 6

m6 = 1 #kg

omega = 19 #deg
omega = np.deg2rad(omega)

M = 1 / np.power(10,5) # 1e-05

#########

# 2.
N = l / (4*a)
print("N = ",N)

m = np.abs(np.sqrt(2) * N * I * np.power(a,2))

print(" |m| = ", m)
#

# 3

tau = np.abs(N * I * np.power(a,2)* B * np.sin(np.deg2rad(90.)))

print("tau = ",tau)

# 4


R = ro * (l/(r*r*np.pi))

print(" R = ",R)

# 5

P = I*I *R

print("P = ",P)

# 6

I = m6 * ((a*a) / 6)

print(" I = ",I)

# 7 wrong

t = omega * (I/M)

print(" t = ", t)