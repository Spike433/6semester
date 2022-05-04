using System;

namespace raytracing
{
    /// <summary>
    /// Klasa predstvlja scenu kod modela crtanja slike pomocu ray tracinga. Sastoji
    /// se od izvora svjetlosti i konacnog broja objekata.
    /// </summary>
    public class Scene
    {
        const int MAXDEPTH = 5; //maksimalna dubina rekurzije
        private int numberOfObjects;
        private Sphere[] sphere;
        private Point lightPosition;
        private ColorVector backgroundColors = new ColorVector(0, 0, 0);
        private ColorVector light = new ColorVector((float)1, (float)1, (float)1);
        private ColorVector ambientLight = new ColorVector((float)0.5, (float)0.5, (float)0.5);

        /// <summary>
        /// Inicijalni konstruktor koji postavlja poziciju svijetla i parametre svih
        /// objekata u sceni.
        /// </summary>
        /// <param name="lightPosition">pozicija svijetla</param>
        /// <param name="numberOfObjects">broj objekata u sceni</param>
        /// <param name="sphereParameters">parametri svih kugli</param>
        public Scene(Point lightPosition, int numberOfObjects, SphereParameters[] sphereParameters)
        {
            this.lightPosition = lightPosition;
            this.numberOfObjects = numberOfObjects;
            sphere = new Sphere[numberOfObjects];
            for (int i = 0; i < numberOfObjects; i++)
            {
                sphere[i] = new Sphere(sphereParameters[i]);
            }
        }

        /// <summary>
        /// Metoda provjerava da li postoji sjena na tocki presjeka. Vraca true ako
        /// se zraka od mjesta presjeka prema izvoru svjetlosti sjece s nekim objektom.
        /// </summary>
        /// <param name="intersection">tocka presjeka</param>
        /// <returns>true ako postoji sjena u tocki presjeka, false ako ne postoji</returns>
        private bool shadow(Point intersection)
        { // todo 4 
            Ray rayForShadow = new Ray(intersection, lightPosition);

            for (int i = 0; i < this.numberOfObjects; i++)
            {
				if (this.sphere[i].intersection(rayForShadow))
				{
					return true;
				}
            }
            return false;
        }

        /// <summary>
        /// Metoda koja pomocu pracenja zrake racuna boju u tocki presjeka. Racuna se
        /// osvjetljenje u tocki presjeka te se zbraja s doprinosima osvjetljenja koje
        /// donosi reflektirana i refraktirana zraka.
        /// </summary>
        /// <param name="ray">pracena zraka</param>
        /// <param name="depth">dubina rekurzije</param>
        /// <returns>vektor boje u tocki presjeka</returns>
        public ColorVector traceRay(Ray ray, int depth)
        { // todo 3
			
			int closestIntersection =-1;
	        bool intersected =false;

	        ColorVector colorVector = new ColorVector(0, 0, 0);

            ColorVector backgroundLocal = backgroundColors;
	        

	        if(MAXDEPTH < depth ) 
            {
		        return  new ColorVector(0,0,0); 
	        }
	        else 
            {
		        for(int i=0; i< this.numberOfObjects; i++) 
                {
			        if(sphere[i].intersection(ray) == true)
                    {
                        intersected = true;

                        closestIntersection = getClosestInterseticion(ray, closestIntersection, i);
                    }
                }  
		  
		    
				if(intersected == true)
                {
                    PropertyVector ambientCoiefitient = sphere[closestIntersection].getKa();
                    double red, green, blue, pom;

                    getColorParams(ambientCoiefitient, out red, out green, out blue);

                    backgroundLocal = new ColorVector((float)red, (float)green, (float)blue);

                    Point closestPoint = sphere[closestIntersection].getIntersectionPoint();

                    PropertyVector diffCofient = sphere[closestIntersection].getKd();

                    Vector N, V, L, R;
                    setupVectors(ray, closestIntersection, closestPoint, out N, out V, out L, out R);

                    V.normalize();
                    R.normalize();
                    L.normalize();

                    double NI = sphere[closestIntersection].getNi();
                    double VN = V.dotProduct(N);
                    if (VN < 0)
                    {
                        N = N.multiple(-1);
                        NI = 1.0 / NI;
                    }

                    bool shadow = this.shadow(closestPoint);

                    if ((pom = L.dotProduct(N)) > 0 && shadow == false)
                    {
                        red = light.getRed() * diffCofient.getRedParameter() * pom;
                        green = light.getGreen() * diffCofient.getGreenParameter() * pom;
                        blue = light.getBlue() * diffCofient.getBlueParameter() * pom;

                        ColorVector diffCdi = new ColorVector((float)red, (float)green, (float)blue);
                        backgroundLocal = backgroundLocal.add(diffCdi);
                    }

                    PropertyVector spectralKoef = sphere[closestIntersection].getKs();

                    pom = R.dotProduct(V);

                    if (pom > 0 && !shadow)
                    {
                        red = light.getRed() * spectralKoef.getRedParameter() * Math.Pow(pom, sphere[closestIntersection].getN());
                        green = light.getGreen() * spectralKoef.getGreenParameter() * Math.Pow(pom, sphere[closestIntersection].getN());
                        blue = light.getBlue() * spectralKoef.getBlueParameter() * Math.Pow(pom, sphere[closestIntersection].getN());

                        ColorVector spectralC = new ColorVector((float)red, (float)green, (float)blue);
                        backgroundLocal = backgroundLocal.add(spectralC);
                    }

                    Vector reflectedV = V.getReflectedVector(N);
                    reflectedV.normalize();

                    Ray reflectionRay = new Ray(closestPoint, reflectedV);
                    ColorVector reflectionColorVector = traceRay(reflectionRay, depth + 1);


                    Vector U = V.getRefractedVector(N, NI);
                    U.normalize();

                    Ray refRay = new Ray(closestPoint, U);
                    ColorVector refrColorVector = traceRay(refRay, depth + 1);

                    colorVector = modelColorVector(closestIntersection, colorVector, backgroundLocal, reflectionColorVector, refrColorVector);

                    return colorVector;
                }
                else 
				{
					return colorVector = new ColorVector(backgroundColors.getRed(), backgroundColors.getBlue(), backgroundColors.getGreen());
				}
			}
        }

        private ColorVector modelColorVector(int closestIntersection, ColorVector colorVector, ColorVector backgroundLocal, ColorVector reflectionColorVector, ColorVector refrColorVector)
        {
            colorVector = colorVector.add(backgroundLocal);

            colorVector = colorVector.add(reflectionColorVector.multiple(sphere[closestIntersection].getReflectionFactor()));
            colorVector = colorVector.add(refrColorVector.multiple(sphere[closestIntersection].getRefractionFactor()));
            colorVector.correct();
            return colorVector;
        }

        private int getClosestInterseticion(Ray ray, int closestIntersection, int i)
        {
            if (closestIntersection == -1)
            {
                closestIntersection = i;
            }
            if (ray.getStartingPoint().getDistanceFrom(sphere[closestIntersection].getIntersectionPoint()) > ray.getStartingPoint().getDistanceFrom(sphere[i].getIntersectionPoint()))
            {
                closestIntersection = i;
            }

            return closestIntersection;
        }

        private void getColorParams(PropertyVector ambientCoiefitient, out double red, out double green, out double blue)
        {
            blue = ambientLight.getBlue() * ambientCoiefitient.getBlueParameter();
            red = ambientLight.getRed() * ambientCoiefitient.getRedParameter();
            green = ambientLight.getGreen() * ambientCoiefitient.getGreenParameter();
        }

        private void setupVectors(Ray ray, int closestIntersection, Point closestPoint, out Vector N, out Vector V, out Vector L, out Vector R)
        {
            N = sphere[closestIntersection].getNormal(closestPoint);
            V = new Vector(closestPoint, ray.getStartingPoint());
            L = new Vector(closestPoint, lightPosition);
            R = L.getReflectedVector(N);
        }
    }
}