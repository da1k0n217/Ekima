export type Station = {
    id: string;
    name: string;
    prefecture: string;
    line: string;
    latitude: number;
    longitude: number;
    postal: string;
    distance: string;
    prev: string;
    next: string;
};

export type ApiStation = {
    name: string;
    prefecture: string;
    line: string;
    x: number;
    y: number;
    postal: string;
    distance: string;
    prev: string;
    next: string;
};

export type StationsApiResponse = {
    response: {
        station: ApiStation[];
    };
};

export function mapApiStation(station: ApiStation): Station {
    return {
        id: `${station.name}-${station.x}-${station.y}`,
        name: station.name,
        prefecture: station.prefecture,
        line: station.line,
        latitude: station.y,
        longitude: station.x,
        postal: station.postal,
        distance: station.distance,
        prev: station.prev,
        next: station.next
    };
}

export function mapApiStations(response: StationsApiResponse): Station[] {
    return response.response.station.map(mapApiStation);
}

export type Coordinates = {
    latitude: number;
    longitude: number;
    accuracy: number;
    timestamp: number;
};

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
