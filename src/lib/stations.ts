export type Station = {
    id: string;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
};

export type Coordinates = {
    latitude: number;
    longitude: number;
    accuracy: number;
    timestamp: number;
};

export const SAMPLE_STATIONS: Station[] = [
    {
        id: 'nagoya',
        name: '名古屋駅',
        address: '愛知県名古屋市中村区名駅',
        latitude: 35.170915,
        longitude: 136.881537
    },
    {
        id: 'sakae',
        name: '栄駅',
        address: '愛知県名古屋市中区栄',
        latitude: 35.170694,
        longitude: 136.908611
    }
];

const EARTH_RADIUS_METERS = 6_371_000;

export function getDistance(
    latitude1: number,
    longitude1: number,
    latitude2: number,
    longitude2: number
): number {
    const toRadians = (degrees: number) => (degrees * Math.PI) / 180;
    const deltaLatitude = toRadians(latitude2 - latitude1);
    const deltaLongitude = toRadians(longitude2 - longitude1);
    const latitude1Radians = toRadians(latitude1);
    const latitude2Radians = toRadians(latitude2);
    const a =
        Math.sin(deltaLatitude / 2) ** 2 +
        Math.cos(latitude1Radians) *
            Math.cos(latitude2Radians) *
            Math.sin(deltaLongitude / 2) ** 2;

    return (
        2 *
        EARTH_RADIUS_METERS *
        Math.atan2(Math.sqrt(a), Math.sqrt(Math.max(0, 1 - a)))
    );
}

export function findNearestStation(
    coordinates: Coordinates,
    stations: Station[]
): { station: Station; distance: number } | null {
    return stations.reduce<{ station: Station; distance: number } | null>(
        (nearest, station) => {
            const distance = getDistance(
                coordinates.latitude,
                coordinates.longitude,
                station.latitude,
                station.longitude
            );
            return nearest === null || distance < nearest.distance
                ? { station, distance }
                : nearest;
        },
        null
    );
}

export function formatDistance(distance: number): string {
    return distance < 1000
        ? `${Math.round(distance)} m`
        : `${(distance / 1000).toFixed(1)} km`;
}
