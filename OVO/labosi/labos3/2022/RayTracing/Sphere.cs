using System;

namespace raytracing
{
    /// <summary>
    /// Klasa predstavlja kuglu u prostoru. Nasljeduje apstraktnu klasu Object. Kugla
    /// je odredena svojim polozajem, radijusom, bojom, parametrima materijala i
    /// udjelima pojedninih zraka (osnovne, odbijene i lomljene).
    /// </summary>
    public class Sphere:Object
    {
        private double radius;
        const double Epsilon = 0.0001;
        private double rayDistanceFromCenter;
        private Point IntersectionPoint;

        /// <summary>
        /// Inicijalni konstruktor koji postavlja sve parametre kugle. Za prijenos
        /// parametara koristi se pomocna klasa SphereParameters.
        /// </summary>
        /// <param name="sphereParameters">parametri kugle</param>
        public Sphere ( SphereParameters sphereParameters )
            : base(sphereParameters.getCenterPosition(), sphereParameters.getRaysContributions(),
                sphereParameters.getMaterialParameters(), sphereParameters.getN(),
                sphereParameters.getNi())
        {
            this.radius = sphereParameters.getRadius();
        }

        /// <summary>
        /// Metoda ispituje postojanje presjeka zrake ray s kuglom. Ako postoji presjek
        /// postavlja tocku presjeka IntersectionPoint, te
        /// vraca logicku vrijednost true.
        /// </summary>
        /// <param name="ray">zraka za koju se ispituje postojanje presjeka sa kuglom</param>
        /// <returns>logicku vrijednost postojanja presjeka zrake s kuglom</returns>
        public override bool intersection (Ray ray)
        { // todo 2
            double alfa = new double();
            double length = new double();


            // create vector from 2 points
            Vector vectorPC = new Vector(ray.getStartingPoint(), this.getCenterPosition());

            length = getLength(vectorPC);

            alfa = ray.getDirection().getAngle(vectorPC);

            if ((alfa * 180.0 / Math.PI) > 90)
                return false;

            double d = new double();
            d = length * Math.Sin(alfa);
            rayDistanceFromCenter = d;

            if (d > this.radius)
                return false;

            double valuePD;
            valuePD = Math.Sqrt(Math.Pow(vectorPC.getLength(), 2) - Math.Pow(d, 2));

            double closerPB = new double();
            double fartherPD = new double();

            closerPB = calculatePointPB(d, valuePD);

            if (closerPB <= (0 + Epsilon))
            {
                double temp = valuePD + (Math.Sqrt(radius * radius - d * d));
                closerPB = temp;
                fartherPD = temp;
            }

            this.IntersectionPoint = new Point(ray.getStartingPoint(), ray.getDirection(), closerPB);

            return true;
        }

        private double calculatePointPB(double d, double valuePD)
        {
            return valuePD - Math.Sqrt(Math.Pow(radius, 2) - Math.Pow(d, 2));
        }

        private static double getLength(Vector vectorPC)
        {
            return Math.Sqrt(Math.Pow(vectorPC.getX(), 2) + Math.Pow(vectorPC.getY(), 2) + Math.Pow(vectorPC.getZ(), 2));
        }

        /// <summary>
        /// Vraca tocku presjeka kugle sa zrakom koja je bliza pocetnoj tocki zrake.
        /// </summary>
        /// <returns>tocka presjeka zrake s kuglom koja je bliza izvoru zrake</returns>
        public override Point getIntersectionPoint ()
        {
            return IntersectionPoint;
        }

        /// <summary>
        /// Vraca normalu na kugli u tocki point
        /// </summary>
        /// <param name="point">point na kojoj se racuna normala na kugli</param>
        /// <returns>normal vektor normale</returns>
        public override Vector getNormal ( Point point )
        { // todo 1
         
            Ray rayForNormal = new Ray(this.centerPosition, point);
            
            return  rayForNormal.getDirection(); 
        }
    }
}
//za svaki x, y u prozoru iscrtavanja
//izračunaj zraku R kroz x, y
//boja C = TraceRay(R,0)
//obojaj točku x, y bojom C
//kraj
//funkcija TraceRay(R, dubina)
//ako je dubina>max. dubine, prekini funkciju i vrati crnu boju
//nađi najbliži presjek zrake R sa scenom
//ako nema presjeka, prekini funkciju i vrati kao rezultat boju pozadine
//izračunaj boju lokalnog osvjetljenja Clocal u točki presjeka
//izračunaj odbijenu zraku Rrefl
//boja Crefl = TraceRay(Rrefl, dubina+1)
//izračunaj refraktiranu zraku Rrefr
//boja Crefr = TraceRay(Rrefr, dubina+1)
//boja C = kombinacija(Clocal, Crefl, Crefr)
//kraj: vrati boju C