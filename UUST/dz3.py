from scipy.stats import norm

H = 384

sredina = 400
devijacija = 100

y_min = 4995
y_max = 5550
x_min = 1185
x_max = 1580

dy = 209
dx = 242

TEC_S1 = 32
TEC_S2 = 35
TEC_S3 = 41
TEC_S4 = 50

f = 1176.45e6

# a) loc je sredina, a scale je devijacija
udio = 1-norm(loc=sredina, scale=devijacija).cdf(H)
print("Postotak ukupnog sadržaja elektrona iznad ISS2: {}".format(udio))

# b)
x_p = x_min+dx
y_p = y_min+dy
print("Koordinate točke P su: x_p = {0} i y_p = {1}".format(x_p, y_p))

# c) formula sa slajda 20
# Potrebno je normalizirati koordinate satelita

x_p_norm = (x_p-x_min)/(x_max-x_min)
y_p_norm = (y_p-y_min)/(y_max-y_min)

TEC = TEC_S1 * x_p_norm * y_p_norm + TEC_S2 * \
    (1-x_p_norm) * y_p_norm + TEC_S3 * (1-x_p_norm) * \
    (1-y_p_norm) + TEC_S4 * x_p_norm * (1-y_p_norm)
print("Ukupni sadržaj elektrona je: {} TECU".format(TEC))

# d) formula sa slajda 17

I_T = 40.3/(f*f*3e8)*1e16 * TEC*1e9
print("Ionosfersko kašnjenje je: {} ns".format(I_T))

# e) Postotak iz a) zadatka pomnožen s I_T
I_T2 = I_T*udio
print("Ionosfersko kašnjenje iznad ISS2 je: {} ns".format(I_T2))

# f) Formula sa slajda 17

I_S = I_T2 * 3e8 * 1e-9
print("Pogreška procijenjene udaljenosti je: {} m".format(I_S))