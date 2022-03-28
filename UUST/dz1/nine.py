import numpy as np
import math
from scipy.optimize import fsolve

# Upiši vektor položaja (r_vect) i vektor brzine (v_vect)
r_vect = np.array([-568.1, 6460.4, -1677.7])
v_vect = np.array([-9.39, 1.51, 3.23])
# Upiši proteklo vrijeme u MINUTAMA za izračun prave anomalije (pod c)
minutes_elapsed = 81.2
delta_t = minutes_elapsed * 60

# Konstante
mi = 398600
earth_radius = 6378.0

# A)
print("******** A) *************")
v = np.linalg.norm(v_vect)
print(" normalizedVector  -->   {}  km/s ".format(v), end= " | ")

r = np.linalg.norm(r_vect)
print("radius --> {} km ".format(r), end=" | ")

v_r = np.dot(r_vect, v_vect) / r
print("vRadius --> {}  km/s ".format(v_r))

h_vect = np.cross(r_vect, v_vect)
print("hVector --> {} km^3/s ".format(h_vect))

h = np.linalg.norm(h_vect)
print("hNormalized -->  {} kg * km^3/s ".format(h), end=" | ")

i = math.acos(h_vect[2] / h)
print("i --> {} radians or {} degrees".format(i, 360*i / (2*math.pi)))

N_vect = np.cross(np.array([0, 0, 1]), h_vect)
print("nVector --> {} ".format(N_vect), end=" | ")

N = np.linalg.norm(N_vect)
print("nNormalized --> {} ".format(N))


e_vect = (np.multiply(v*v - mi/r, r_vect) - np.multiply(r*v_r, v_vect)) / mi
print("eVector -->  {}".format(e_vect))

OMEGA = math.acos(N_vect[0] / N) if N_vect[1] >= 0.0 else 2*math.pi - math.acos(N_vect[0] / N)
print("omega -->  {} radians  or  {} degress".format(OMEGA, 360*OMEGA / (2*math.pi)))

e = np.linalg.norm(e_vect)
print("e --> {}".format(e))

theta = math.acos(np.dot(e_vect, r_vect) / (e*r)) if v_r >= 0.0 else 2*math.pi - math.acos(np.dot(e_vect, r_vect) / (e*r))
print("theta --> {} radians  or {} degrees".format(theta, 360*theta / (2*math.pi)))

omega = math.acos(np.dot(N_vect, e_vect) / (N*e)) if e_vect[2] >= 0.0 else 2*math.pi - math.acos(np.dot(N_vect, e_vect) / (N*e))
print("w --> {} radians -->  {} degrees".format(omega, 360*omega / (2*math.pi)))


# B)
print("\n")
print("********* B) ***********")
r_perigee = (h*h) / (mi * (1 + e))
print("rPerigee --> {} km ".format(r_perigee), end=" | ")

r_apogee = (h*h) / (mi * (1 - e))

z_min = r_perigee - earth_radius

r_max = r_apogee

print("rMaxDistance --> {} km".format(r_max), end=" | ")
print("zMinDistance -->  {} km".format(z_min))





# C)
print('\n')

print("********* C) **************")

E_0 = math.atan( math.tan(theta/2) * math.sqrt((1-e)/(1+e)) ) * 2
print("e0 -->  {} radians".format(E_0))

a = 0.5 * (r_perigee + r_apogee)
print("a -->  {} km".format(a), end= " | ")

T = 2*math.pi * math.pow(a, 1.5) / math.sqrt(mi)
print("t -->  {} s".format(T), end= " | ")

M_0 = E_0 - e * math.sin(E_0)
print("m0 --> {} radians".format(M_0))

t_0 = M_0 * T / (2 * math.pi)
print("t0 --> {}".format(t_0), end=" | ")

t_f = t_0 + delta_t
print("tF --> {}".format(t_f), end=" | ")

M_after = 2 * math.pi * t_f / T
print("mAfter --> {} radians".format(M_after))

def f(x):
    return x - e * math.sin(x) - M_after
E_after = fsolve(f, 1)
print("eAfter -->  {} radians".format(E_after), end= " | ")

omega_after = math.atan( math.tan(E_after/2) * math.sqrt( (1+e) / (1-e) ) ) * 2
print("omegaAfter -->  {} degrees or  {} radains".format(math.degrees(omega_after), omega_after))