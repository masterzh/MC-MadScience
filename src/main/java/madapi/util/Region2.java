package madapi.util;

import universalelectricity.api.vector.Vector2;

public class Region2
{
        private Vector2 min;
        private Vector2 max;

        public Region2()
        {
                this(new Vector2(), new Vector2());
        }

        private Region2(Vector2 min, Vector2 max)
        {
                this.min = min;
                this.max = max;
        }

// TODO Remove unused code found by UCDetector
//         /**
//          * Checks if a point is located inside a region
//          */
//         public boolean isIn(Vector2 point)
//         {
//                 return (point.x > this.min.x && point.x < this.max.x) && (point.y > this.min.y && point.y < this.max.y);
//         }

        
}