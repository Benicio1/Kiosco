using System;

namespace AuraPaint.Models
{
    public struct StrokePoint
    {
        public float X;
        public float Y;
        public float Pressure;
        public float TiltX;
        public float TiltY;
        public long Timestamp;

        public StrokePoint(float x, float y, float pressure, float tiltX = 0, float tiltY = 0, long timestamp = 0)
        {
            X = x;
            Y = y;
            Pressure = pressure;
            TiltX = tiltX;
            TiltY = tiltY;
            Timestamp = timestamp;
        }
    }
}
