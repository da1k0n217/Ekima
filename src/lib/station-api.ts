import {
    mapApiStation,
    type Coordinates,
    type Station,
    type StationsApiResponse
} from './stations';

const API_URL = 'https://express.heartrails.com/api/json?method=getStations';

export async function fetchStations(coordinates: Coordinates): Promise<Station[]> {
    const params = new URLSearchParams({ 
        x: String(coordinates.longitude),
        y: String(coordinates.latitude),
    });

    const res = await fetch(`${API_URL}&${params}`);

    if (!res.ok) {
        throw new Error('取得に失敗');
    }

    const data = (await res.json()) as StationsApiResponse;
    return data.response.station.map(mapApiStation);
}
