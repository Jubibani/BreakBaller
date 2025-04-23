using UnityEngine;

public class LevelGenerator : MonoBehaviour
{
    public Vector2Int size;          // reference the size value input from the editor 
    public Vector2 offset;           // reference the offset value input from the editor 
    public GameObject brickPrefab;   // brickPrefab is to reference the 'brick' object that has been made as a prefab

    //starts the code after the script has been loaded, even before the 'Start()' function
    private void Awake()
    {
        GenerateLevel();
    }

    void GenerateLevel()
    {
        // Loop through rows and columns to create the grid
        for (int row = 0; row < size.x; row++) // Outer loop for rows
        {
            for (int col = 0; col < size.y; col++) // Inner loop for columns
            {
                GameObject newBrick = Instantiate(brickPrefab, transform);
                newBrick.transform.position = transform.position + new Vector3((float)((size.x-1)*.5f-row) * offset.x, col * offset.y, 0);
            }
        }
    }
}
