using UnityEngine;

public class LevelGenerator : MonoBehaviour
{
    public Vector2Int size;          // Number of rows (x) and columns (y) for the grid
    public Vector2 offset;           // Spacing between bricks in the grid
    public GameObject brickPrefab;   // Reference to the brick prefab

    public Gradient gradient;
    private void Awake()
    {
        GenerateLevel();
    }

    void GenerateLevel()
    {
        for (int row = 0; row < size.x; row++) // Outer loop for rows
        {
            for (int col = 0; col < size.y; col++) // Inner loop for columns
            {
                // Calculate the position of each brick
                float xPosition = (row - (size.x - 1) * 0.5f) * offset.x; // Centered horizontal position
                float yPosition = col * offset.y;                        // Vertical position
                Vector3 position = transform.position + new Vector3(xPosition, yPosition, 0);

                // Instantiate the brick prefab at the calculated position
                GameObject newBrick = Instantiate(brickPrefab, position, Quaternion.identity, transform);
                newBrick.GetComponent<SpriteRenderer>().color = gradient.Evaluate((float)col / (size.y - 1));
            }
        }

        Debug.Log($"Generated a grid of {size.x} rows and {size.y} columns.");
    }

}